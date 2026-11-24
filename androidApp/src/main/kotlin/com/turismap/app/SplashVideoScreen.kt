package com.turismap.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun SplashVideoScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val colorCremaVideo = remember { AndroidColor.parseColor("#F3EFE4") }
    var navegado by remember { mutableStateOf(false) }

    fun finalizarSplash() {
        if (!navegado) {
            navegado = true
            onSplashFinished()
        }
    }

    // Pinta toda la ventana y barras del sistema con el fondo crema del video
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        val prevStatusBar = window?.statusBarColor
        val prevNavBar = window?.navigationBarColor
        window?.statusBarColor = colorCremaVideo
        window?.navigationBarColor = colorCremaVideo
        window?.decorView?.setBackgroundColor(colorCremaVideo)

        onDispose {
            if (prevStatusBar != null) window.statusBarColor = prevStatusBar
            if (prevNavBar != null) window.navigationBarColor = prevNavBar
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.splash_turismap}")
            setMediaItem(MediaItem.fromUri(videoUri))
            volume = 0f
            playbackParameters = PlaybackParameters(2.5f)
            repeatMode = Player.REPEAT_MODE_OFF
            prepare()
            playWhenReady = true
        }
    }

    LaunchedEffect(Unit) {
        delay(4600L)
        finalizarSplash()
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    finalizarSplash()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            val rootLayout = FrameLayout(ctx).apply {
                setBackgroundColor(colorCremaVideo)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setOnClickListener { finalizarSplash() }
            }

            val playerView = PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setBackgroundColor(colorCremaVideo)
                setShutterBackgroundColor(colorCremaVideo)
                // Amplía el logo Aprovechando los márgenes internos del video 16:9
                scaleX = 1.75f
                scaleY = 1.75f
            }

            val hintView = TextView(ctx).apply {
                text = "Toca para continuar"
                setTextColor(AndroidColor.parseColor("#7C786D"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                gravity = Gravity.CENTER
            }

            val hintParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = (36 * ctx.resources.displayMetrics.density).toInt()
            }

            rootLayout.addView(
                playerView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            rootLayout.addView(hintView, hintParams)
            rootLayout
        }
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}