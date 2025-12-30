package com.zashed.videoeditor

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.effect.Contrast
import androidx.media3.effect.RgbFilter
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.zashed.videoeditor.databinding.ActivityMainBinding
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var player: ExoPlayer? = null
    private var selectedVideoUri: Uri? = null
    private var videoDuration: Long = 0
    private var startTime: Long = 0
    private var endTime: Long = 0

    private val selectVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedVideoUri = uri
                initializePlayer(uri)
                binding.tvNoVideo.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        checkPermissions()
    }

    private fun setupClickListeners() {
        binding.btnSelectVideo.setOnClickListener {
            selectVideo()
        }

        binding.btnTrim.setOnClickListener {
            toggleTrimControls()
        }

        binding.btnFilter.setOnClickListener {
            applyFilter()
        }

        binding.btnSave.setOnClickListener {
            saveVideo()
        }

        binding.btnApplyTrim.setOnClickListener {
            applyTrim()
        }

        setupSeekBars()
    }

    private fun setupSeekBars() {
        binding.seekBarStart.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && videoDuration > 0) {
                    startTime = (progress.toFloat() / 100 * videoDuration).toLong()
                    binding.tvStartTime.text = formatTime(startTime)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekBarEnd.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && videoDuration > 0) {
                    endTime = (progress.toFloat() / 100 * videoDuration).toLong()
                    binding.tvEndTime.text = formatTime(endTime)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_VIDEO
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 100)
        }
    }

    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        selectVideoLauncher.launch(intent)
    }

    private fun initializePlayer(uri: Uri) {
        player?.release()
        player = ExoPlayer.Builder(this).build()

        val mediaItem = MediaItem.fromUri(uri)
        player?.setMediaItem(mediaItem)
        player?.prepare()

        binding.playerView.player = player

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    videoDuration = player?.duration ?: 0
                    endTime = videoDuration
                    binding.tvVideoDuration.text = formatTime(videoDuration)
                    binding.tvEndTime.text = formatTime(endTime)
                    binding.seekBarEnd.progress = 100
                }
            }
        })
    }

    private fun toggleTrimControls() {
        val visibility = if (binding.trimControls.visibility == android.view.View.VISIBLE) {
            android.view.View.GONE
        } else {
            android.view.View.VISIBLE
        }
        binding.trimControls.visibility = visibility
    }

    private fun applyTrim() {
        if (selectedVideoUri == null) {
            Toast.makeText(this, "Selecione um vídeo primeiro", Toast.LENGTH_SHORT).show()
            return
        }

        if (startTime >= endTime) {
            Toast.makeText(this, "O tempo inicial deve ser menor que o final", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Cortando vídeo...", Toast.LENGTH_SHORT).show()

        val inputMediaItem = MediaItem.fromUri(selectedVideoUri!!)
        val outputFile = File(getExternalFilesDir(null), "trimmed_video_${System.currentTimeMillis()}.mp4")

        val transformer = Transformer.Builder(this)
            .setVideoMimeType("video/mp4")
            .build()

        transformer.startTransformation(inputMediaItem, outputFile.absolutePath)

        transformer.addListener(object : Transformer.Listener {
            override fun onCompleted(composition: androidx.media3.transformer.Composition, result: ExportResult) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Vídeo cortado com sucesso!", Toast.LENGTH_SHORT).show()
                    selectedVideoUri = Uri.fromFile(outputFile)
                    initializePlayer(selectedVideoUri!!)
                }
            }

            override fun onError(composition: androidx.media3.transformer.Composition, result: ExportResult, exception: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Erro ao cortar vídeo: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun applyFilter() {
        if (selectedVideoUri == null) {
            Toast.makeText(this, "Selecione um vídeo primeiro", Toast.LENGTH_SHORT).show()
            return
        }

        val filters = arrayOf("Sepia", " Preto e Branco", "Contraste Alto", "Sem Filtro")

        AlertDialog.Builder(this)
            .setTitle("Escolha um filtro")
            .setItems(filters) { _, which ->
                applySelectedFilter(which)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun applySelectedFilter(filterIndex: Int) {
        Toast.makeText(this, "Aplicando filtro...", Toast.LENGTH_SHORT).show()

        val inputMediaItem = MediaItem.fromUri(selectedVideoUri!!)
        val outputFile = File(getExternalFilesDir(null), "filtered_video_${System.currentTimeMillis()}.mp4")

        val effects = when (filterIndex) {
            0 -> Effects(listOf(), listOf(RgbFilter.createSepiaFilter()))
            1 -> Effects(listOf(), listOf(RgbFilter.createGrayscaleFilter()))
            2 -> Effects(listOf(), listOf(Contrast(2.0f)))
            else -> Effects(listOf(), listOf())
        }

        val transformer = Transformer.Builder(this)
            .setVideoMimeType("video/mp4")
            .setEffects(effects)
            .build()

        transformer.startTransformation(inputMediaItem, outputFile.absolutePath)

        transformer.addListener(object : Transformer.Listener {
            override fun onCompleted(composition: Composition, result: ExportResult) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Filtro aplicado com sucesso!", Toast.LENGTH_SHORT).show()
                    selectedVideoUri = Uri.fromFile(outputFile)
                    initializePlayer(selectedVideoUri!!)
                }
            }

            override fun onError(composition: Composition, result: ExportResult, exception: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Erro ao aplicar filtro: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun saveVideo() {
        if (selectedVideoUri == null) {
            Toast.makeText(this, "Selecione um vídeo primeiro", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val inputStream = contentResolver.openInputStream(selectedVideoUri!!)
            val outputFile = File(getExternalFilesDir(null), "final_video_${System.currentTimeMillis()}.mp4")

            inputStream?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Adicionar à galeria
            val values = android.content.ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, "VideoEditado_${System.currentTimeMillis()}.mp4")
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VideoEditor")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }

            val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val itemUri = contentResolver.insert(collection, values)

            itemUri?.let { uri ->
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                contentResolver.update(uri, values, null, null)
            }

            Toast.makeText(this, getString(R.string.video_saved), Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, "${getString(R.string.error_saving_video)}: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
