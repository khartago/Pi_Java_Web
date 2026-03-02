# FARMTECH - Demo images for plants and planting
# Run from project root: .\create_demo_images.ps1
# Copies plant images from target/classes/images (run 'mvn compile' first)

$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot
$gameImagesDir = Join-Path $projectRoot "target" "classes" "images"

# -----------------------------------------------------------------------------
# 1. PROBLEME PHOTOS (uploads/problemes/) - plant disease/pest context
# Use project plant images: tomato/potato leaves, dead plant for blight
# -----------------------------------------------------------------------------
$problemesDir = Join-Path (Join-Path $projectRoot "uploads") "problemes"
if (-not (Test-Path $problemesDir)) { New-Item -ItemType Directory -Path $problemesDir -Force | Out-Null }

$problemeSources = @(
    @{ dest = "demo_1_0.png"; src = "tomato.2.png" },
    @{ dest = "demo_1_1.png"; src = "tomato.1.png" },
    @{ dest = "demo_2_0.png"; src = "papper.1.png" },
    @{ dest = "demo_4_0.png"; src = "dead.png" }
)

foreach ($item in $problemeSources) {
    $destPath = Join-Path $problemesDir $item.dest
    $srcPath = Join-Path $gameImagesDir $item.src
    if (Test-Path $srcPath) {
        Copy-Item $srcPath $destPath -Force
        Write-Host "Copied $($item.src) -> $($item.dest)"
    } else {
        Write-Warning "Run 'mvn compile' first. Missing: $srcPath"
    }
}

# -----------------------------------------------------------------------------
# 2. PRODUIT IMAGES (uploads/produits/) - tomatoes, potatoes, peppers, etc.
# -----------------------------------------------------------------------------
$produitsDir = Join-Path (Join-Path $projectRoot "uploads") "produits"
if (-not (Test-Path $produitsDir)) { New-Item -ItemType Directory -Path $produitsDir -Force | Out-Null }

$produitSources = @(
    @{ dest = "tomates_cerises.png"; src = "tomato.3.png" },
    @{ dest = "pommes_de_terre.png"; src = "potato.3.png" },
    @{ dest = "poivrons.png"; src = "papper.3.png" },
    @{ dest = "engrais.png"; src = "manure.png" },
    @{ dest = "fongicide.png"; src = "manure.png" },
    @{ dest = "semences_ble.png"; src = "potato.1.png" },
    @{ dest = "plants_tomate.png"; src = "tomato.1.png" },
    @{ dest = "semences_mais.png"; src = "potato.2.png" }
)

foreach ($item in $produitSources) {
    $destPath = Join-Path $produitsDir $item.dest
    $srcPath = Join-Path $gameImagesDir $item.src
    if (Test-Path $srcPath) {
        Copy-Item $srcPath $destPath -Force
        Write-Host "Copied $($item.src) -> $($item.dest)"
    } else {
        Write-Warning "Missing: $srcPath"
    }
}

Write-Host ""
Write-Host "Demo images ready:"
Write-Host "  - uploads/problemes/ (probleme photos)"
Write-Host "  - uploads/produits/ (produit images)"
