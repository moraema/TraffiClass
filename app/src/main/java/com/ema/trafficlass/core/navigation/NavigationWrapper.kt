package com.ema.trafficlass.core.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ema.trafficlass.capturePeatonal.presentation.view.TraffitScreen
import com.ema.trafficlass.capturePeatonal.presentation.viewModel.traffitViewModel
import com.ema.trafficlass.home.presentation.view.HomeScreen

@Composable
fun NavigationWrapper(){
    val navController = rememberNavController()
    val homeViewModel: traffitViewModel = viewModel()

    NavHost(navController = navController, startDestination = Home) {
        composable<Home> { HomeScreen(
            onHometraffit = {navController.navigate(TraffitHome)}
        ) }

        composable<TraffitHome> { TraffitScreen(
            homeViewModel = homeViewModel,
            onLogout = {navController.navigate(Home)}
        ) }
    }
}