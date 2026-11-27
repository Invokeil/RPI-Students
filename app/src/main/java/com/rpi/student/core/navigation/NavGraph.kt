package com.rpi.student.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rpi.student.data.local.UserEntity
import com.rpi.student.data.remote.Appwrite
import com.rpi.student.data.remote.AuthRepo
import com.rpi.student.features.academic.AcademicHubScreen
import com.rpi.student.features.academic.CreditBrowserScreen
import com.rpi.student.features.academic.ExamRoutineScreen
import com.rpi.student.features.academic.ResultsScreen
import com.rpi.student.features.academic.RoutineScreen
import com.rpi.student.features.academic.GpaCalculatorScreen
import com.rpi.student.features.academic.CgpaCalculatorScreen
import com.rpi.student.features.auth.AcademicFormScreen
import com.rpi.student.features.auth.LoginScreen
import com.rpi.student.features.auth.OtpScreen
import com.rpi.student.features.auth.SignupScreen
import com.rpi.student.features.auth.SignupState
import com.rpi.student.features.auth.SplashScreen
import com.rpi.student.features.auth.VerificationPendingScreen
import com.rpi.student.features.bus.BusScreen
import com.rpi.student.features.chat.ChatRoomScreen
import com.rpi.student.features.chat.GroupListScreen
import com.rpi.student.features.developer.DeveloperScreen
import com.rpi.student.features.home.HomeScreen
import com.rpi.student.features.mess.BazarScreen
import com.rpi.student.features.mess.DepositsScreen
import com.rpi.student.features.mess.MessEntryScreen
import com.rpi.student.features.mess.MessScreen
import com.rpi.student.features.mess.MonthlyBillScreen
import com.rpi.student.features.productivity.FitnessScreen
import com.rpi.student.features.productivity.PomodoroScreen
import com.rpi.student.features.profile.AboutScreen
import com.rpi.student.features.profile.EditRequestScreen
import com.rpi.student.features.profile.ProfileScreen
import com.rpi.student.features.profile.SettingsScreen
import com.rpi.student.core.ui.MainShell

/**
 * সম্পূর্ণ navigation — app-এর সব page এখানে wire করা।
 * Flow: Splash → Signup(create account) → Academic form → OTP → Profile save → Pending → Main tabs।
 * Login: password session → HOME।
 */
@Composable
fun RpiNavHost() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: ""

    fun toHomeClearingAuth() {
        nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
    }

    NavHost(navController = nav, startDestination = Routes.SPLASH) {
        // ---------- Root ----------
        composable(Routes.SPLASH) {
            SplashScreen(onDone = { nav.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } } })
        }

        // ---------- Auth ----------
        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { toHomeClearingAuth() },
                onGoSignup = { nav.navigate(Routes.SIGNUP) },
            )
        }
        composable(Routes.SIGNUP) {
            SignupScreen(
                onNext = { nav.navigate(Routes.ACADEMIC_FORM) },
                onGoLogin = { nav.popBackStack() },
            )
        }
        composable(Routes.ACADEMIC_FORM) {
            AcademicFormScreen(
                onSubmit = { email -> nav.navigate(Routes.otp(email)) },
                onSkipToPending = { toHomeClearingAuth() },
            )
        }
        composable(
            Routes.OTP,
            arguments = listOf(navArgument("email") { type = NavType.StringType; defaultValue = "" }),
        ) { entry ->
            OtpScreen(
                email = entry.arguments?.getString("email") ?: "",
                onVerified = { nav.navigate(Routes.PENDING) },
                onFallback = { toHomeClearingAuth() },
            )
        }
        composable(Routes.PENDING) {
            VerificationPendingScreen(onContinueDemo = { toHomeClearingAuth() })
        }

        // ---------- Main tabs (glass bottom bar) ----------
        composable(Routes.HOME) {
            MainShell(currentRoute, { r -> nav.tabTo(r) }) { HomeScreen(onNavigate = { nav.navigate(it) }) }
        }
        composable(Routes.ACADEMIC) {
            MainShell(currentRoute, { r -> nav.tabTo(r) }) { AcademicHubScreen(onNavigate = { nav.navigate(it) }) }
        }
        composable(Routes.CHAT_LIST) {
            MainShell(currentRoute, { r -> nav.tabTo(r) }) { GroupListScreen(onOpenGroup = { nav.navigate(Routes.chatRoom(it)) }) }
        }
        composable(Routes.MESS) {
            MainShell(currentRoute, { r -> nav.tabTo(r) }) { MessScreen(onNavigate = { nav.navigate(it) }) }
        }
        composable(Routes.PROFILE) {
            MainShell(currentRoute, { r -> nav.tabTo(r) }) {
                ProfileScreen(
                    onNavigate = { nav.navigate(it) },
                    onLogout = {
                        nav.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        // ---------- Academic ----------
        composable(Routes.ROUTINE) { RoutineScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.RESULTS) { ResultsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.GPA) { GpaCalculatorScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.CGPA) { CgpaCalculatorScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.CREDITS) { CreditBrowserScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.EXAM) { ExamRoutineScreen(onBack = { nav.popBackStack() }) }

        // ---------- Bus ----------
        composable(Routes.BUS) { BusScreen(onBack = { nav.popBackStack() }) }

        // ---------- Chat ----------
        composable(
            Routes.CHAT_ROOM,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType; defaultValue = "g-own" }),
        ) { entry ->
            ChatRoomScreen(groupId = entry.arguments?.getString("groupId") ?: "g-own", onBack = { nav.popBackStack() })
        }

        // ---------- Mess ----------
        composable(Routes.MESS_ENTRY) { MessEntryScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.BAZAR) { BazarScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.DEPOSITS) { DepositsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.MONTHLY_BILL) { MonthlyBillScreen(onBack = { nav.popBackStack() }) }

        // ---------- Productivity ----------
        composable(Routes.POMODORO) { PomodoroScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.FITNESS) { FitnessScreen(onBack = { nav.popBackStack() }) }

        // ---------- Profile ----------
        composable(Routes.SETTINGS) { SettingsScreen(onBack = { nav.popBackStack() }, onNavigate = { nav.navigate(it) }) }
        composable(Routes.EDIT_REQUEST) { EditRequestScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.ABOUT) { AboutScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.DEVELOPER) { DeveloperScreen(onBack = { nav.popBackStack() }) }
    }
}

private fun NavController.tabTo(route: String) =
    navigate(route) { launchSingleTop = true; restoreState = true }
