# KMP Clean Architecture — Prompt de Reconstruction

> Ce document sert de **prompt d'ingénierie** pour reproduire la même structure d'un projet Kotlin Multiplatform (KMP) partageant la logique métier (UseCases/domain) entre Android (Jetpack Compose) et iOS (SwiftUI natif).

---

## Contexte & Objectif

Crée un projet **Kotlin Multiplatform (KMP)** avec la structure suivante :
- Un module `shared` contenant toute la logique métier (domain + data) compilée pour Android **et** iOS.
- Un module `androidApp` avec une UI en **Jetpack Compose** et des `ViewModel` AndroidX.
- Une app `iosApp` en **SwiftUI natif** qui consomme directement les `UseCase` Kotlin depuis le framework partagé.
- L'injection de dépendances est faite avec **Koin** des deux côtés.
- La couche réseau utilise **Ktor** (multiplatform).
- Les clés secrètes (API keys, URLs) sont lues depuis `local.properties` via **BuildKonfig**.
- L'architecture suit le pattern **Clean Architecture** : `data → domain → presentation`.
- L'app Android utilise le mode **edge-to-edge** (Android 15+).

---

## 1. Structure des dossiers

```
MyApp/
├── build.gradle.kts                    ← plugins racine (versions des plugins)
├── settings.gradle.kts                 ← include des modules
├── gradle.properties
├── local.properties                    ← secrets (API_KEY, BASE_URL) — jamais commité
│
├── shared/                             ← MODULE KMP PARTAGÉ
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/example/myapp/
│       │   ├── data/
│       │   │   ├── api/
│       │   │   │   ├── KtorApi.kt          ← classe abstraite base Ktor (lit BuildKonfig)
│       │   │   │   └── MyService.kt        ← appels HTTP concrets
│       │   │   ├── datasource/
│       │   │   │   └── RemoteDataSource.kt ← interface
│       │   │   ├── datasourceimpl/
│       │   │   │   └── RemoteDataSourceImpl.kt
│       │   │   ├── model/
│       │   │   │   ├── MyApiResponse.kt    ← DTO (Serializable)
│       │   │   │   └── MyRemote.kt         ← DTO entité (Serializable)
│       │   │   ├── mapper/
│       │   │   │   └── Mapper.kt           ← extension fun Remote.toDomain()
│       │   │   └── repository/
│       │   │       └── MyRepositoryImpl.kt
│       │   ├── domain/
│       │   │   ├── model/
│       │   │   │   └── MyEntity.kt         ← modèle domaine pur
│       │   │   ├── repository/
│       │   │   │   └── MyRepository.kt     ← interface internal
│       │   │   └── usecases/
│       │   │       ├── GetItemsUseCase.kt  ← KoinComponent + inject
│       │   │       └── GetItemUseCase.kt
│       │   ├── di/
│       │   │   └── SharedModule.kt         ← modules Koin partagés
│       │   └── util/
│       │       └── Dispatcher.kt           ← expect/actual Dispatcher
│       │
│       ├── androidMain/kotlin/com/example/myapp/util/
│       │   └── Dispatcher.kt               ← actual Android (Dispatchers.IO)
│       │
│       └── iosMain/kotlin/com/example/myapp/
│           └── util/
│               ├── Dispatcher.kt           ← actual iOS (Dispatchers.Default)
│               └── KoinHelper.kt           ← fun initKoin() exposée à Swift
│
└── androidApp/                         ← MODULE ANDROID
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/example/myapp/android/
            ├── MyApplication.kt            ← startKoin (appModule + sharedModule)
            ├── MainActivity.kt             ← enableEdgeToEdge() avant setContent
            ├── AppSetup.kt                 ← NavHost + Scaffold racine
            ├── di/
            │   └── AppModule.kt            ← viewModel { ... }
            ├── common/
            │   ├── Destination.kt          ← sealed/objects de navigation
            │   └── MyAppBar.kt             ← statusBarsPadding() sur le Row intérieur
            ├── home/
            │   ├── HomeScreen.kt
            │   ├── HomeState.kt
            │   └── HomeViewModel.kt
            ├── detail/
            │   ├── DetailScreen.kt
            │   ├── DetailState.kt
            │   └── DetailViewModel.kt
            └── ui/theme/
                └── MyApplicationTheme.kt

iosApp/
└── iosApp/
    ├── iOSApp.swift                    ← @main, init Koin
    ├── home/
    │   ├── HomeScreen.swift
    │   ├── HomeViewModel.swift         ← extension HomeScreen { @MainActor class ... }
    │   └── MyGridItem.swift
    ├── detail/
    │   └── DetailScreen.swift
    └── util/
        └── Sample.swift                ← données de preview
```

---

## 2. Dépendances complètes

### `build.gradle.kts` (racine)

```kotlin
plugins {
    id("com.android.application").version("8.5.1").apply(false)
    id("com.android.library").version("8.5.1").apply(false)
    kotlin("android").version("2.1.20").apply(false)
    kotlin("multiplatform").version("2.1.20").apply(false)
    // Déclaré ici pour que le plugin de sérialisation hérite de la version dans les sous-modules
    kotlin("plugin.serialization").version("2.1.20").apply(false)
    // Kotlin 2.0+ : remplace kotlinCompilerExtensionVersion dans composeOptions
    // Sa version DOIT toujours être identique à la version de Kotlin
    id("org.jetbrains.kotlin.plugin.compose").version("2.1.20").apply(false)
    // Injecte les valeurs de local.properties dans un objet BuildKonfig accessible en commonMain
    id("com.codingfeline.buildkonfig").version("0.17.1").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
```

### `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "MyApp"
include(":androidApp")
include(":shared")
```

### `local.properties` (jamais commité — dans `.gitignore`)

```properties
# SDK Android (généré automatiquement par Android Studio)
sdk.dir=/Users/yourname/Library/Android/sdk

# Clés secrètes lues par BuildKonfig au moment du build
API_KEY=your_api_key_here
BASE_URL=https://api.example.com
```

### `shared/build.gradle.kts`

```kotlin
import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    // Pas de version ici : hérite de kotlin("plugin.serialization").version("2.1.20") du root
    kotlin("plugin.serialization")
    id("com.codingfeline.buildkonfig") version "0.17.1"
}

// ← APRÈS le bloc plugins {} (contrainte Gradle Kotlin DSL)
val localProperties = Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) load(propsFile.inputStream())
}

kotlin {
    // androidTarget {} depuis KMP 1.9+ (remplace android {} dans le bloc kotlin)
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "1.8" }
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "shared" }
    }

    val ktorVersion      = "2.2.4"
    val coroutineVersion = "1.6.4"
    val koinVersion      = "3.3.2"

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                api("io.insert-koin:koin-core:$koinVersion")
            }
        }
        val commonTest by getting {
            dependencies { implementation(kotlin("test")) }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:$ktorVersion")
                api("io.insert-koin:koin-android:$koinVersion")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

// BuildKonfig génère un objet BuildKonfig accessible depuis commonMain (et iOS via le framework)
buildkonfig {
    packageName = "com.example.myapp"

    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "API_KEY",
            localProperties.getProperty("API_KEY")
                ?: error("API_KEY est absent de local.properties")
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "BASE_URL",
            localProperties.getProperty("BASE_URL")
                ?: error("BASE_URL est absent de local.properties")
        )
    }
}

android {
    namespace = "com.example.myapp"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
```

### `androidApp/build.gradle.kts`

```kotlin
plugins {
    id("com.android.application")
    kotlin("android")
    // Kotlin 2.0+ : gère le Compose Compiler — version héritée du root (2.1.20)
    // Remplace composeOptions { kotlinCompilerExtensionVersion } qui n'existe plus
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.myapp.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.myapp.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    // NE PAS ajouter composeOptions { kotlinCompilerExtensionVersion } avec Kotlin 2.0+
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
        jniLibs {
            // Compatibilité avec les appareils Android à pages de 16 Ko (Android 15+)
            useLegacyPackaging = true
        }
    }
    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
    val composeVersion     = "1.7.8"   // compatible Kotlin 2.x / Compose Compiler 2.x
    val koinComposeVersion = "3.5.6"
    val coilVersion        = "2.7.0"
    val accompanistVersion = "0.36.0"  // uniquement pour les utilitaires non-systemUiController
    val navVersion         = "2.8.6"

    implementation(project(":shared"))
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("io.insert-koin:koin-androidx-compose:$koinComposeVersion")
    implementation("io.coil-kt:coil-compose:$coilVersion")
    implementation("androidx.navigation:navigation-compose:$navVersion")
}
```

---

## 3. Couche `shared` — Code complet

### 3.1 `util/Dispatcher.kt` (commonMain) — pattern `expect/actual`

```kotlin
// commonMain
internal interface Dispatcher {
    val io: CoroutineDispatcher
}
internal expect fun provideDispatcher(): Dispatcher

// androidMain
internal class AndroidDispatcher : Dispatcher {
    override val io: CoroutineDispatcher get() = Dispatchers.IO
}
internal actual fun provideDispatcher(): Dispatcher = AndroidDispatcher()

// iosMain
internal class IosDispatcher : Dispatcher {
    override val io: CoroutineDispatcher get() = Dispatchers.Default
}
internal actual fun provideDispatcher(): Dispatcher = IosDispatcher()
```

> **Pourquoi ?** iOS ne supporte pas `Dispatchers.IO`, on utilise `Dispatchers.Default`.

### 3.2 `data/api/KtorApi.kt` — Classe abstraite base HTTP + BuildKonfig

```kotlin
package com.example.myapp.data.api

import com.example.myapp.BuildKonfig  // ← généré automatiquement par BuildKonfig Gradle plugin
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.http.path
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal abstract class KtorApi {
    protected val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    fun HttpRequestBuilder.pathUrl(path: String) {
        url {
            takeFrom(BuildKonfig.BASE_URL) // ← lu depuis local.properties via BuildKonfig
            path("3", path)
            parameter("api_key", BuildKonfig.API_KEY) // ← idem
        }
    }
}
```

> **Pourquoi BuildKonfig ?** `BuildConfig` d'Android n'est disponible que dans `androidMain`.
> `BuildKonfig` (plugin `com.codingfeline.buildkonfig`) génère un objet accessible depuis
> `commonMain`, donc partagé entre Android et iOS — aucune constante hardcodée dans le code.

### 3.3 `data/api/MyService.kt`

```kotlin
internal class MyService : KtorApi() {
    suspend fun getItems(page: Int = 1): MyApiResponse = client.get {
        pathUrl("item/popular")
        parameter("page", page)
    }.body()

    suspend fun getItem(id: Int): MyRemote = client.get {
        pathUrl("item/$id")
    }.body()
}
```

### 3.4 DTOs — `data/model/`

```kotlin
// MyApiResponse.kt
@Serializable
data class MyApiResponse(
    val results: List<MyRemote>
)

// MyRemote.kt
@Serializable
data class MyRemote(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("poster_path") val posterImage: String,
    @SerialName("release_date") val releaseDate: String,
)
```

### 3.5 `data/mapper/Mapper.kt`

```kotlin
internal fun MyRemote.toDomain(): MyEntity = MyEntity(
    id = id,
    title = title,
    overview = overview,
    posterImage = buildImageUrl(posterImage),
    releaseDate = releaseDate,
)

private fun buildImageUrl(path: String): String =
    "https://image.tmdb.org/t/p/w500/$path"
```

### 3.6 `domain/model/MyEntity.kt`

```kotlin
// Modèle domaine pur — pas d'annotation, pas de dépendance externe
data class MyEntity(
    val id: Int,
    val title: String,
    val overview: String,
    val posterImage: String,
    val releaseDate: String,
)
```

### 3.7 `domain/repository/MyRepository.kt`

```kotlin
// internal = non exposé à l'extérieur du module shared
internal interface MyRepository {
    suspend fun getItems(page: Int = 1): List<MyEntity>
    suspend fun getItem(id: Int): MyEntity
}
```

### 3.8 UseCases — LE POINT CLÉ DU PARTAGE iOS/Android

```kotlin
// GetItemsUseCase.kt (commonMain)
// Utilise KoinComponent pour s'auto-injecter — fonctionne en Kotlin natif ET depuis Swift
class GetItemsUseCase : KoinComponent {
    private val repository by inject<MyRepository>()

    @Throws(Exception::class)                  // expose l'exception à Swift
    suspend operator fun invoke(page: Int) = repository.getItems(page)
}

// GetItemUseCase.kt
class GetItemUseCase : KoinComponent {
    private val repository by inject<MyRepository>()

    @Throws(Exception::class)
    suspend operator fun invoke(id: Int) = repository.getItem(id)
}
```

> **Clé de l'architecture** : Les UseCases héritent de `KoinComponent` et utilisent `inject<>()` au lieu de recevoir le repository en constructeur. Ainsi, depuis Swift, on peut faire `GetItemsUseCase.init()` sans passer de paramètre — Koin résout tout automatiquement.

### 3.9 `di/SharedModule.kt`

```kotlin
private val dataModule = module {
    factory { MyService() }
    factory<RemoteDataSource> { RemoteDataSourceImpl(get(), get()) }
}

private val utilityModule = module {
    factory { provideDispatcher() }
}

private val domainModule = module {
    single<MyRepository> { MyRepositoryImpl(get()) }
    factory { GetItemsUseCase() }
    factory { GetItemUseCase() }
}

private val _sharedModule = listOf(dataModule, utilityModule, domainModule)
fun getSharedModule() = _sharedModule
```

### 3.10 `iosMain/util/KoinHelper.kt` — Pont Kotlin → Swift

```kotlin
// Cette fonction est exposée à Swift sous le nom KoinHelperKt.doInitKoin()
fun initKoin() {
    startKoin {
        modules(getSharedModule())
    }
}
```

---

## 4. Module `androidApp`

### 4.1 Application + DI

```kotlin
// MyApplication.kt
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule + getSharedModule())
        }
    }
}

// di/AppModule.kt
val appModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { params ->
        DetailViewModel(
            getItemUseCase = get(),
            itemId = params.get()   // paramètre passé depuis la navigation
        )
    }
}
```

### 4.2 `MainActivity.kt` — Edge-to-edge

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ← DOIT être appelé AVANT setContent
        // Permet au layout de s'étendre derrière la status bar ET la navigation bar
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AppSetup()
                }
            }
        }
    }
}
```

> **Important** : `enableEdgeToEdge()` rend la status bar et la navigation bar transparentes.
> Il faut gérer les insets manuellement dans les composants (voir `MyAppBar` et `AppSetup`).
> Ne pas utiliser `accompanist-systemuicontroller` avec edge-to-edge — ils sont en conflit.

### 4.3 Navigation — `common/Destination.kt`

```kotlin
interface Destination {
    val title: String
    val route: String
    val routeWithArgs: String
}

object Home : Destination {
    override val title = "Items"
    override val route = "home"
    override val routeWithArgs = route
}

object Detail : Destination {
    override val title = "Item details"
    override val route = "detail"
    override val routeWithArgs = "$route/{itemId}"
    val arguments = listOf(navArgument("itemId") { type = NavType.IntType })
}

val appDestinations = listOf(Home, Detail)
```

### 4.4 `common/MyAppBar.kt` — Gestion du padding status bar

```kotlin
@Composable
fun MyAppBar(
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean,
    currentScreen: Destination,
    onNavigateBack: () -> Unit
) {
    Surface(
        // Pas de height fixe sur la Surface : elle s'adapte à son contenu
        // (hauteur de la status bar + 56dp du Row)
        modifier = modifier.fillMaxWidth(),
        elevation = 4.dp,
        color = MaterialTheme.colors.primary
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding() // ← KEY : ajoute un padding top = hauteur de la status bar
                .fillMaxWidth()      //         la Surface (couleur primary) s'étend derrière
                .height(56.dp)       //         hauteur logique du contenu (titre + bouton back)
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = canNavigateBack,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colors.onBackground,
                    )
                }
            }
            Text(
                text = currentScreen.title,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}
```

> **Pattern edge-to-edge** :
> - La `Surface` n'a **pas** de `height` fixe → elle grandit pour inclure la zone de la status bar.
> - `statusBarsPadding()` sur le `Row` → le contenu (titre, bouton) se positionne **sous** la status bar.
> - La couleur `primary` de la `Surface` remplit visuellement la zone derrière la status bar transparente.

### 4.5 `AppSetup.kt` — NavHost racine + padding navigation bar

```kotlin
@Composable
fun AppSetup() {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = appDestinations.find {
        backStackEntry?.destination?.route == it.route ||
                backStackEntry?.destination?.route == it.routeWithArgs
    } ?: Home

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MyAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    ) { innerPaddings ->
        NavHost(
            navController = navController,
            startDestination = Home.routeWithArgs,
            modifier = Modifier
                .padding(innerPaddings)     // ← padding top fourni par la TopAppBar
                .navigationBarsPadding()    // ← évite que le contenu passe derrière la nav bar
        ) {
            composable(Home.routeWithArgs) {
                val vm: HomeViewModel = koinViewModel()
                HomeScreen(
                    uiState = vm.uiState,
                    loadNextItems = { vm.loadItems(forceReload = false) },
                    navigateToDetail = { item ->
                        navController.navigate("${Detail.route}/${item.id}")
                    }
                )
            }
            composable(Detail.routeWithArgs) { backStack ->
                val itemId = backStack.arguments?.getString("itemId") ?: "0"
                val vm: DetailViewModel = koinViewModel(
                    parameters = { parametersOf(itemId.toInt()) }
                )
                DetailScreen(uiState = vm.uiState)
            }
        }
    }
}
```

### 4.6 ViewModel Android — Pattern `mutableStateOf`

```kotlin
// HomeState.kt
data class HomeState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val items: List<MyEntity> = emptyList(),
    val errorMessage: String? = null,
    val loadFinished: Boolean = false,
)

// HomeViewModel.kt
class HomeViewModel(
    private val getItemsUseCase: GetItemsUseCase
) : ViewModel() {

    var uiState by mutableStateOf(HomeState())
        private set
    private var currentPage = 1

    init { loadItems(forceReload = false) }

    fun loadItems(forceReload: Boolean = false) {
        if (uiState.loading) return
        if (forceReload) currentPage = 1
        if (currentPage == 1) uiState = uiState.copy(refreshing = true)

        viewModelScope.launch {
            uiState = uiState.copy(loading = true)
            try {
                val result = getItemsUseCase(currentPage)
                val items = if (currentPage == 1) result else uiState.items + result
                currentPage++
                uiState = uiState.copy(
                    loading = false, refreshing = false,
                    loadFinished = result.isEmpty(), items = items
                )
            } catch (e: Throwable) {
                uiState = uiState.copy(
                    loading = false, refreshing = false,
                    loadFinished = true, errorMessage = e.message
                )
            }
        }
    }
}
```

---

## 5. Module `iosApp` (SwiftUI natif)

### 5.1 `iOSApp.swift` — Point d'entrée + init Koin

```swift
import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        // Appelle la fun Kotlin `initKoin()` du KoinHelper.kt
        KoinHelperKt.doInitKoin()
    }
    var body: some Scene {
        WindowGroup { HomeScreen() }
    }
}
```

### 5.2 ViewModel iOS — Pattern `extension` + `@MainActor ObservableObject`

```swift
// HomeViewModel.swift — classe interne à HomeScreen via extension
extension HomeScreen {
    @MainActor class HomeViewModel: ObservableObject {

        // Instancie le UseCase Kotlin directement (Koin résout les dépendances)
        private let getItemsUseCase = GetItemsUseCase.init()

        @Published private(set) var items: [MyEntity] = []
        @Published private(set) var isLoading: Bool = false
        private var currentPage: Int32 = 1
        private(set) var loadFinished: Bool = false

        func loadItems() async {
            guard !isLoading else { return }
            isLoading = true
            do {
                let result = try await getItemsUseCase.invoke(page: currentPage)
                isLoading = false
                loadFinished = result.isEmpty
                currentPage += 1
                self.items += result
            } catch {
                isLoading = false
                loadFinished = true
                print(error.localizedDescription)
            }
        }
    }
}
```

> **Pourquoi `extension HomeScreen` ?** Co-localise le ViewModel avec sa View. Convention Swift idiomatique pour les `ObservableObject` liés à une seule View.

### 5.3 `HomeScreen.swift`

```swift
struct HomeScreen: View {
    @StateObject var viewModel = HomeViewModel()
    let gridColumns = Array(repeating: GridItem(.flexible(), spacing: 16), count: 2)

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVGrid(columns: gridColumns, spacing: 16) {
                    ForEach(viewModel.items, id: \.id) { item in
                        NavigationLink(value: item) {
                            MyGridItem(item: item)
                                .task {
                                    // Pagination : charge quand on arrive au dernier élément
                                    if item == viewModel.items.last
                                        && !viewModel.isLoading
                                        && !viewModel.loadFinished {
                                        await viewModel.loadItems()
                                    }
                                }
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                    if viewModel.isLoading {
                        Section(footer: ProgressView()) {}
                    }
                }
                .padding(.horizontal, 12)
                .navigationDestination(for: MyEntity.self) { item in
                    DetailScreen(item: item)
                }
            }
            .navigationTitle("Items")
        }
        .task { await viewModel.loadItems() }
    }
}
```

---

## 6. Règles d'architecture à respecter

| Règle | Description |
|-------|-------------|
| **Visibilité `internal`** | `Repository`, `DataSource`, `RepositoryImpl`, `DataSourceImpl`, `KtorApi`, `Dispatcher` sont tous `internal` dans `shared`. Seuls les `UseCase` et le modèle domaine (`MyEntity`) sont publics. |
| **UseCase = KoinComponent** | Chaque UseCase hérite de `KoinComponent` et utilise `by inject<>()` — Swift peut alors faire `.init()` sans argument, Koin résout tout. |
| **Dispatcher expect/actual** | Toujours abstraire le dispatcher — iOS n'a pas `Dispatchers.IO`, on utilise `Dispatchers.Default`. |
| **`@Throws(Exception::class)`** | Annoter les `suspend fun` exposées à Swift pour que Swift puisse les `try/catch`. |
| **State = data class immuable** | Les `XxxState` sont des `data class` avec `copy()` — jamais de mutation directe. |
| **ViewModel Android ≠ ViewModel iOS** | Android : `AndroidX ViewModel` + `viewModelScope`. iOS : `@MainActor ObservableObject` + `async/await`. Mais **les deux consomment les mêmes UseCases Kotlin**. |
| **Mapper en extension fun** | Le mapper `Remote.toDomain()` est une `internal fun` dans `data/mapper` — jamais dans le DTO lui-même. |
| **Modèle domaine pur** | `MyEntity` n'a aucune annotation (`@Serializable`, etc.) et aucune dépendance externe. |
| **BuildKonfig pour les secrets** | Les clés API et URLs sont dans `local.properties` (jamais commité). BuildKonfig génère `BuildKonfig.API_KEY` / `BuildKonfig.BASE_URL` accessibles depuis `commonMain`. |
| **Kotlin 2.0+ : plugin Compose** | `id("org.jetbrains.kotlin.plugin.compose")` remplace `composeOptions { kotlinCompilerExtensionVersion }`. Sa version = version de Kotlin. |
| **`androidTarget {}` dans `kotlin {}`** | Depuis KMP 1.9+, utiliser `androidTarget {}` à la place de `android {}` dans le bloc `kotlin {}` du module shared. |
| **Edge-to-edge** | `enableEdgeToEdge()` dans `MainActivity` avant `setContent`. `statusBarsPadding()` sur le `Row` de l'AppBar. `navigationBarsPadding()` sur le `NavHost`. Ne pas utiliser `accompanist-systemuicontroller` (conflit). |

---

## 7. Flux de données complet

```
[local.properties]
    ↓ BuildKonfig plugin (build time)
[BuildKonfig.API_KEY / BASE_URL]   ← objet généré dans commonMain
    ↓
[KtorApi.pathUrl()]

[API REST]
    ↓ Ktor HttpClient
[MyService.kt]              ← appels HTTP, retourne DTOs
    ↓
[RemoteDataSourceImpl]      ← withContext(dispatcher.io) { ... }
    ↓
[MyRepositoryImpl]          ← .map { remote.toDomain() }
    ↓
[MyRepository]              ← interface (inversion de dépendance)
    ↓
[GetItemsUseCase]           ← KoinComponent, @Throws, suspend operator fun invoke()
    ↓
[Android ViewModel]         ← viewModelScope.launch { useCase(page) }
[iOS ViewModel]             ← Task { try await useCase.invoke(page: page) }
    ↓
[HomeState / @Published]
    ↓
[Compose UI / SwiftUI]
```

---

## 8. Gestion edge-to-edge (Android 15+)

```
┌─────────────────────────────────┐  ← Status bar (transparente grâce à enableEdgeToEdge)
│  Surface(color = primary)       │  ← Zone colorée derrière la status bar
│  ┌─────────────────────────────┐│
│  │ Row.statusBarsPadding()     ││  ← Contenu poussé sous la status bar (56dp)
│  │  [←] Titre de l'écran      ││
│  └─────────────────────────────┘│
├─────────────────────────────────┤
│                                 │
│   NavHost content               │  ← Padding top via Scaffold innerPaddings
│   .padding(innerPaddings)       │
│   .navigationBarsPadding()      │  ← Contenu ne passe pas sous la nav bar
│                                 │
├─────────────────────────────────┤  ← Navigation bar (transparente)
└─────────────────────────────────┘
```

**Règle** : `accompanist-systemuicontroller.setStatusBarColor()` est **incompatible** avec `enableEdgeToEdge()` — les supprimer ensemble.

---

## 9. Checklist de configuration Xcode (iOS)

1. Ouvrir `iosApp/iosApp.xcodeproj` (ou `.xcworkspace`).
2. Dans **Build Phases → Link Binary With Libraries**, ajouter `shared.framework`.
3. Dans **Build Settings**, s'assurer que le framework est bien cherché dans le dossier de build Gradle.
4. Le script `embedAndSignAppleFrameworkForXcode` doit être configuré dans Gradle pour copier le framework dans le build Xcode.
5. `import shared` dans chaque fichier Swift qui utilise le code Kotlin.

---

## 10. Commandes utiles

```bash
# Compiler le framework iOS (depuis la racine du projet)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Builder l'app Android
./gradlew :androidApp:assembleDebug

# Nettoyer
./gradlew clean
```

---

## 11. Extensions possibles

- **Persistance locale** : utiliser `SQLDelight` (multiplatform) dans `shared/commonMain` pour un cache local.
- **ViewModels partagés** : déplacer les ViewModels dans `shared` en utilisant `StateFlow` + `KoinComponent` pour les partager aussi côté iOS (pattern plus avancé).
- **Tests** : dans `commonTest`, tester les `UseCase` et `Repository` avec `runTest { }` de `kotlinx-coroutines-test`.
- **Version Catalog** : migrer les dépendances vers `gradle/libs.versions.toml` pour une gestion centralisée.
- **CI/CD** : injecter `API_KEY` et `BASE_URL` via des variables d'environnement GitHub Actions / Bitrise au lieu de `local.properties`.
