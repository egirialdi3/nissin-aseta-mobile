import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.aseta.app.ui.screen.asset_viewer.AssetViewerScreen
import id.aseta.app.ui.screen.asset_viewer.RFIDAssetViewerScreen
import id.aseta.app.ui.screen.corrective_maintenance.CorrectiveMaintenanceScreen
import id.aseta.app.ui.screen.find_asset.FindAssetScreen
import id.aseta.app.ui.screen.find_asset.FindingAssetScreen
import id.aseta.app.ui.screen.inspection.InspectionScreen
import id.aseta.app.ui.screen.mutasi.MutationScreen
import id.aseta.app.ui.screen.stock_opname.NewStockOpnameScreen
import id.aseta.app.ui.screen.rfid_scan.RFIDScanScreen
import id.aseta.app.ui.screen.stock_opname.RFIDScanStockOpnameScreen
import id.aseta.app.ui.screen.relocation.RelocationScreen
import id.aseta.app.ui.screen.stock_opname.ScanAssetScreen
import id.aseta.app.ui.screen.qr_scan.ScanQRScreen
import id.aseta.app.ui.screen.rfid_registration.RfidRegistrationDetailPage
import id.aseta.app.ui.screen.rfid_registration.RfidRegistrationPage
import id.aseta.app.ui.screen.rfid_replace.RfidReplaceTagDetailPage
import id.aseta.app.ui.screen.rfid_replace.RfidReplaceTagPage
import id.aseta.app.ui.screen.rfid_replace.RfidReplaceTagScan
import id.aseta.app.ui.screen.rfid_replace.RfidReplaceTagViewerScreen
import id.aseta.app.ui.screen.stock_opname.StockOpnameListScreen
import id.aseta.app.viewmodel.AssetCategoryViewModel
import id.aseta.app.viewmodel.AssetViewerViewModel
import id.aseta.app.viewmodel.UHFViewModel

@Composable
fun AsetaNavGraph(viewModel: AuthViewModel,uhfViewModel: UHFViewModel,assetViewerModel: AssetViewerViewModel,assetCategoryViewModel: AssetCategoryViewModel) {
    val navController = rememberNavController()
    val startDestination = if (viewModel.isLoggedIn) "home" else "login"
    NavHost(navController,  startDestination = startDestination) {
        composable("login") {
            LoginScreen(viewModel) {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
        composable("home") {
            HomeScreen(viewModel,uhfViewModel,assetCategoryViewModel,navController) {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
        composable("asset_viewer"){
            AssetViewerScreen(viewModel,uhfViewModel,assetCategoryViewModel,navController)
        }
        composable("scan_rfid_asset_viewer"){
            RFIDAssetViewerScreen(assetCategoryViewModel,navController)
        }
        composable(
            route = "scan_qr/{param}"
        ) { backStackEntry ->
            val assetViewerParam = backStackEntry.arguments?.getString("param") ?: ""
            ScanQRScreen(
                navController = navController,
                viewModel = assetViewerModel,
                assetCategoryViewModel = assetCategoryViewModel,
                type = assetViewerParam
            )
        }
        composable("relocation"){
            RelocationScreen(viewModel,uhfViewModel,assetCategoryViewModel,navController)
        }
        composable("relocation_rfid/{param}"){
            backStackEntry ->
            val param = backStackEntry.arguments?.getString("param") ?: ""
            RFIDScanScreen(assetCategoryViewModel,navController,type = param)
        }

        composable("inspection"){
            InspectionScreen(viewModel,uhfViewModel,assetCategoryViewModel,navController)
        }
        composable("stockopname"){
            StockOpnameListScreen(navController,assetCategoryViewModel)
        }

        composable("add_stockopname"){
            NewStockOpnameScreen(viewModel,uhfViewModel,assetCategoryViewModel,navController)
        }

        composable(
            route = "scan_asset/{param}"
        ) { backStackEntry ->
            val assetViewerParam = backStackEntry.arguments?.getString("param") ?: ""
            ScanAssetScreen(
                navController = navController,
                viewModel = assetViewerModel,
                assetCategoryViewModel = assetCategoryViewModel,
                type = assetViewerParam
            )
        }

        composable(
            route = "scan_asset_rfid/{param}"
        ) { backStackEntry ->
            val assetViewerParam = backStackEntry.arguments?.getString("param") ?: ""
            RFIDScanStockOpnameScreen(
                navController = navController,
                viewModel = assetViewerModel,
                assetCategoryViewModel = assetCategoryViewModel,
                type = assetViewerParam
            )
        }

        composable("find_asset"){
            FindAssetScreen(viewModel,uhfViewModel,assetCategoryViewModel,navController)

        }

        composable("finding_asset_screen/{epcTarget}")
            { backStackEntry ->
                val assetViewerParam = backStackEntry.arguments?.getString("epcTarget") ?: ""
                FindingAssetScreen(assetViewerParam,uhfViewModel,navController,assetCategoryViewModel)
            }

        // tambah rfid registration
        composable("rfid_registration") {
            RfidRegistrationPage(viewModel,uhfViewModel,assetCategoryViewModel,navController)
        }

        composable("rfid_registration_detail") {
            RfidRegistrationDetailPage(viewModel,uhfViewModel,assetCategoryViewModel,navController)
        }

        composable("rfid_replace_tag"){
            RfidReplaceTagViewerScreen(viewModel, uhfViewModel, assetCategoryViewModel, navController)
        }

        composable("rfid_replace_tag_scan"){
            RfidReplaceTagScan(assetCategoryViewModel,navController)
        }

        composable("rfid_replace_tag_page") {
            RfidReplaceTagPage(viewModel,uhfViewModel,assetCategoryViewModel,navController)
        }

        composable("rfid_replace_tag_detail") {
            RfidReplaceTagDetailPage(viewModel,uhfViewModel,assetCategoryViewModel,navController)
        }
        // added for enterprise
        composable("mutation"){
            MutationScreen(viewModel,uhfViewModel,assetCategoryViewModel,navController)
        }

        composable("corrective_maintenance"){
            CorrectiveMaintenanceScreen(viewModel,uhfViewModel,assetCategoryViewModel,navController)

        }
    }
}
