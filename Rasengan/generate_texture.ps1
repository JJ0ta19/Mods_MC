Add-Type -AssemblyName System.Drawing

$outDir = "src/main/resources/assets/rasengan/textures/entity"
$outPath = "$outDir/rasengan_sphere.png"

if (-not (Test-Path -LiteralPath $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

$size = 128
$bmp = New-Object System.Drawing.Bitmap($size, $size)
$gfx = [System.Drawing.Graphics]::FromImage($bmp)
$gfx.SmoothingMode = 'HighQuality'
$gfx.InterpolationMode = 'HighQualityBicubic'
$gfx.PixelOffsetMode = 'HighQuality'
$gfx.Clear([System.Drawing.Color]::FromArgb(0,0,0,0))

$cx = $size / 2
$cy = $size / 2
$maxR = 52
$rand = [System.Random]::new()

# --- Core glow (solid white center) ---
for ($r = 1; $r -le 14; $r++) {
    $alpha = [Math]::Max(0, [Math]::Min(255, 255 - ($r / 14) * 60))
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb($alpha, 255, 255, 255))
    $gfx.FillEllipse($brush, $cx - $r, $cy - $r, $r * 2, $r * 2)
    $brush.Dispose()
}

# --- Inner cyan glow ---
for ($r = 15; $r -le 28; $r++) {
    $t = ($r - 15) / (28 - 15)
    $alpha = [Math]::Max(0, [Math]::Min(255, 200 - $t * 120))
    $cyan = 255 - $t * 80
    $blue = 200 - $t * 50
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb($alpha, 0, [Math]::Max(0, $cyan), [Math]::Max(0, $blue)))
    $gfx.FillEllipse($brush, $cx - $r, $cy - $r, $r * 2, $r * 2)
    $brush.Dispose()
}

# --- Outer glow (blue with transparency) ---
for ($r = 29; $r -le $maxR; $r++) {
    $t = ($r - 29) / ($maxR - 29)
    $alpha = [Math]::Max(0, [Math]::Min(180, 160 - $t * 140))
    $b = [Math]::Max(0, 180 - $t * 80)
    $g = [Math]::Max(0, 100 - $t * 60)
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb($alpha, 0, $g, $b))
    $gfx.FillEllipse($brush, $cx - $r, $cy - $r, $r * 2, $r * 2)
    $brush.Dispose()
}

# --- Swirl layers (spiral arms) ---
$swirlColors = @(
    @(255, 255, 255, 200),   # white core streak
    @(0, 230, 255, 180),     # bright cyan
    @(0, 180, 255, 140),     # electric blue
    @(0, 100, 220, 100),     # deep blue
    @(0, 200, 255, 60)       # faint cyan outer
)

for ($layer = 0; $layer -lt 5; $layer++) {
    $col = $swirlColors[$layer]
    $rBase = 6 + $layer * 9
    $rW = 4 + $layer * 2
    $arms = 2 + $layer
    $points = 60 + $layer * 20
    $thickness = [Math]::Max(1, 4 - $layer * 0.5)

    for ($a = 0; $a -lt $arms; $a++) {
        $angleOffset = ($a / $arms) * [Math]::PI * 2
        $prevX = $null
        $prevY = $null

        for ($i = 0; $i -le $points; $i++) {
            $t = $i / $points
            $angle = $t * [Math]::PI * 4 + $angleOffset
            $dist = $rBase + $t * ($maxR - $rBase - 4)
            $x = $cx + [Math]::Cos($angle) * $dist
            $y = $cy + [Math]::Sin($angle) * $dist

            if ($prevX -ne $null) {
                $alpha2 = [Math]::Max(0, [Math]::Min(255, $col[3] * (1 - $t * 0.6)))
                $pen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb($alpha2, $col[0], $col[1], $col[2]))
                $pen.Width = [float]$thickness
                $pen.EndCap = 'Round'
                $gfx.DrawLine($pen, $prevX, $prevY, $x, $y)
                $pen.Dispose()
            }
            $prevX = $x
            $prevY = $y
        }
    }
}

# --- Energy distortion / electric arcs (thin bright lines) ---
for ($arc = 0; $arc -lt 30; $arc++) {
    $angle = $rand.NextDouble() * [Math]::PI * 2
    $dist = 8 + $rand.NextDouble() * ($maxR - 12)
    $x1 = $cx + [Math]::Cos($angle) * $dist
    $y1 = $cy + [Math]::Sin($angle) * $dist
    $angle2 = $angle + ($rand.NextDouble() - 0.5) * 0.8
    $dist2 = $dist + ($rand.NextDouble() - 0.5) * 15
    $x2 = $cx + [Math]::Cos($angle2) * $dist2
    $y2 = $cy + [Math]::Sin($angle2) * $dist2
    $alphaArc = [Math]::Max(0, [Math]::Min(200, 80 + $rand.Next(60)))
    $pen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb($alphaArc, 180, 255, 255))
    $pen.Width = [float](0.5 + $rand.NextDouble())
    $gfx.DrawLine($pen, $x1, $y1, $x2, $y2)
    $pen.Dispose()
}

# --- Outer particles (floating sparks around sphere) ---
$particleCount = 60
for ($p = 0; $p -lt $particleCount; $p++) {
    $angle = $rand.NextDouble() * [Math]::PI * 2
    $dist = $maxR + 2 + $rand.NextDouble() * 14
    $x = $cx + [Math]::Cos($angle) * $dist
    $y = $cy + [Math]::Sin($angle) * $dist

    $sizeP = 0.5 + $rand.NextDouble() * 2.5
    $alphaP = [Math]::Max(0, [Math]::Min(220, 80 + $rand.Next(140)))

    $bright = 150 + $rand.Next(105)
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb($alphaP, $bright, [Math]::Min(255, $bright + 50), 255))
    $gfx.FillEllipse($brush, $x - $sizeP / 2, $y - $sizeP / 2, $sizeP, $sizeP)
    $brush.Dispose()
}

# --- Extra tiny sparkles (pixel dust) ---
for ($p = 0; $p -lt 80; $p++) {
    $angle = $rand.NextDouble() * [Math]::PI * 2
    $dist = 4 + $rand.NextDouble() * ($maxR + 10)
    $x = $cx + [Math]::Cos($angle) * $dist
    $y = $cy + [Math]::Sin($angle) * $dist
    $alphaP2 = [Math]::Max(0, [Math]::Min(200, 40 + $rand.Next(120)))
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb($alphaP2, 200, 255, 255))
    $gfx.FillRectangle($brush, [Math]::Round($x), [Math]::Round($y), 1, 1)
    $brush.Dispose()
}

# --- Soft outer aura (subtle transparent glow around sphere) ---
for ($r = $maxR + 1; $r -le $maxR + 12; $r++) {
    $t = ($r - $maxR - 1) / 12
    $alpha = [Math]::Max(0, [Math]::Min(80, 60 - $t * 60))
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb($alpha, 0, 100, 255))
    $gfx.FillEllipse($brush, $cx - $r, $cy - $r, $r * 2, $r * 2)
    $brush.Dispose()
}

$gfx.Dispose()
$bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()

Write-Output "Texture generated: $((Get-Item $outPath).Length) bytes at $outPath"
