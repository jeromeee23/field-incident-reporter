package com.example.incidentreporter.presentation.navigation

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.incidentreporter.presentation.createincident.CreateIncidentScreen
import com.example.incidentreporter.presentation.createincident.IncidentCreatedScreen
import com.example.incidentreporter.presentation.incidentlist.home.HomeScreen

internal object AppRoutes {
    const val HOME = "home"
    const val CREATE_INCIDENT = "createincident"
    const val INCIDENT_ID_ARG = "incidentId"
    const val INCIDENT_CREATED = "incidentcreated/{$INCIDENT_ID_ARG}"

    fun incidentCreated(incidentId: String): String {
        return "incidentcreated/${Uri.encode(incidentId)}"
    }
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
    ) {
        composable(AppRoutes.HOME) {
            ProvideHiltViewModelStoreOwner {
                HomeScreen(
                    onCreateReportClick = {
                        navController.navigate(AppRoutes.CREATE_INCIDENT)
                    },
                )
            }
        }
        composable(AppRoutes.CREATE_INCIDENT) {
            ProvideHiltViewModelStoreOwner {
                CreateIncidentScreen(
                    onBack = { navController.popBackStack() },
                    onIncidentCreated = { incidentId ->
                        navController.navigate(AppRoutes.incidentCreated(incidentId)) {
                            popUpTo(AppRoutes.CREATE_INCIDENT) { inclusive = true }
                        }
                    },
                )
            }
        }
        composable(
            route = AppRoutes.INCIDENT_CREATED,
            arguments = listOf(
                navArgument(AppRoutes.INCIDENT_ID_ARG) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val incidentId = backStackEntry.arguments
                ?.getString(AppRoutes.INCIDENT_ID_ARG)
                .orEmpty()
            IncidentCreatedScreen(
                incidentId = incidentId,
                onBackToHome = {
                    navController.popBackStack(AppRoutes.HOME, inclusive = false)
                },
            )
        }
    }
}

@Composable
private fun ProvideHiltViewModelStoreOwner(
    content: @Composable () -> Unit,
) {
    val currentOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val activity = LocalContext.current as ComponentActivity
    val owner = remember(currentOwner, activity) {
        HiltViewModelStoreOwner(currentOwner, activity)
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner, content = content)
}

private class HiltViewModelStoreOwner(
    private val owner: ViewModelStoreOwner,
    private val activity: ComponentActivity,
) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
    override val viewModelStore: ViewModelStore
        get() = owner.viewModelStore

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = activity.defaultViewModelProviderFactory

    override val defaultViewModelCreationExtras: CreationExtras
        get() = activity.defaultViewModelCreationExtras
}
