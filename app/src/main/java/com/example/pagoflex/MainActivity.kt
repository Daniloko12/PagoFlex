package com.example.pagoflex

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.navigation.AppNavegacion
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.PagoFlexViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val pagoFlexViewModel: PagoFlexViewModel = viewModel()
            val modoOscuro by pagoFlexViewModel.modoOscuro.collectAsStateWithLifecycle()

            DisposableEffect(modoOscuro) {
                val estilo = if (modoOscuro) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
                onDispose { }
            }

            PagoFlexTheme(modoOscuro = modoOscuro) {
                AppNavegacion(pagoFlexViewModel)
            }
        }
    }
}
