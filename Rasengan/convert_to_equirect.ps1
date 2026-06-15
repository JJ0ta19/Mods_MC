Add-Type -AssemblyName System.Drawing

$srcPath = "src/main/resources/assets/rasengan/textures/entity/ChatGPT Image 25 may 2026, 01_46_44.png"
$outPath = "src/main/resources/assets/rasengan/textures/entity/rasengan_sphere.png"

$srcImg = [System.Drawing.Image]::FromFile((Get-Item $srcPath).FullName)
$srcBmp = New-Object System.Drawing.Bitmap($srcImg)
$srcImg.Dispose()
$sw = $srcBmp.Width
$sh = $srcBmp.Height

$outW = 256
$outH = 128
$outBmp = New-Object System.Drawing.Bitmap($outW, $outH)
$gfx = [System.Drawing.Graphics]::FromImage($outBmp)
$gfx.InterpolationMode = 'HighQualityBicubic'

$cx = $sw / 2
$cy = $sh / 2
$radius = [Math]::Min($cx, $cy) * 0.85

for ($y = 0; $y -lt $outH; $y++) {
    for ($x = 0; $x -lt $outW; $x++) {
        $lon = ($x / $outW) * 2 * [Math]::PI
        $lat = ($y / $outH) * [Math]::PI

        $dx = [Math]::Sin($lat) * [Math]::Cos($lon)
        $dy = [Math]::Cos($lat)
        $dz = [Math]::Sin($lat) * [Math]::Sin($lon)

        # sample from source image
        $sx = $cx + $dx * $radius
        $sy = $cy + $dy * $radius

        if ($sx -ge 0 -and $sx -lt $sw -and $sy -ge 0 -and $sy -lt $sh) {
            # front hemisphere: sample from ChatGPT image based on (dx, dy)
            $px = [Math]::Round($sx)
            $py = [Math]::Round($sy)
            $px = [Math]::Max(0, [Math]::Min($sw - 1, $px))
            $py = [Math]::Max(0, [Math]::Min($sh - 1, $py))
            $color = $srcBmp.GetPixel($px, $py)

            # if the sample is mostly transparent/black background, fill with gradient
            if ($color.A -lt 10 -and $color.R -lt 20 -and $color.G -lt 20 -and $color.B -lt 20) {
                # Back hemisphere: mirror front
                $backLon = $lon + [Math]::PI
                $backDx = [Math]::Sin($lat) * [Math]::Cos($backLon)
                $backDy = [Math]::Cos($lat)
                $bsx = $cx + $backDx * $radius
                $bsy = $cy + $backDy * $radius
                $bpx = [Math]::Max(0, [Math]::Min($sw - 1, [Math]::Round($bsx)))
                $bpy = [Math]::Max(0, [Math]::Min($sh - 1, [Math]::Round($bsy)))
                $bColor = $srcBmp.GetPixel($bpx, $bpy)
                if ($bColor.A -ge 10) {
                    $color = [System.Drawing.Color]::FromArgb(255, $bColor.R, $bColor.G, $bColor.B)
                } else {
                    # fallback: blue gradient
                    $t = $dy * 0.5 + 0.5
                    $alpha = [Math]::Max(0, [Math]::Min(255, 100 + $t * 80))
                    $r = [Math]::Max(0, [Math]::Min(255, 20 + $t * 40))
                    $g = [Math]::Max(0, [Math]::Min(255, 80 + $t * 60))
                    $b = [Math]::Max(0, [Math]::Min(255, 150 + $t * 80))
                    $color = [System.Drawing.Color]::FromArgb($alpha, $r, $g, $b)
                }
            }
        } else {
            # Outside source area: mirror front hemisphere
            $backLon = $lon + [Math]::PI
            $backDx = [Math]::Sin($lat) * [Math]::Cos($backLon)
            $backDy = [Math]::Cos($lat)
            $bsx = $cx + $backDx * $radius
            $bsy = $cy + $backDy * $radius
            $bpx = [Math]::Max(0, [Math]::Min($sw - 1, [Math]::Round($bsx)))
            $bpy = [Math]::Max(0, [Math]::Min($sh - 1, [Math]::Round($bsy)))
            $bColor = $srcBmp.GetPixel($bpx, $bpy)
            if ($bColor.A -ge 10) {
                $color = [System.Drawing.Color]::FromArgb(255, $bColor.R, $bColor.G, $bColor.B)
            } else {
                $t = $dy * 0.5 + 0.5
                $alpha = [Math]::Max(0, [Math]::Min(255, 80 + $t * 60))
                $r = [Math]::Max(0, [Math]::Min(255, 10 + $t * 30))
                $g = [Math]::Max(0, [Math]::Min(255, 60 + $t * 50))
                $b = [Math]::Max(0, [Math]::Min(255, 130 + $t * 70))
                $color = [System.Drawing.Color]::FromArgb($alpha, $r, $g, $b)
            }
        }

        # blend edges for seamlessness
        $latBlend = [Math]::Sin($lat)
        $blendWeight = 1.0
        if ($lat -lt 0.1) { $blendWeight = $lat / 0.1 }
        if ($lat -gt ([Math]::PI - 0.1)) { $blendWeight = ([Math]::PI - $lat) / 0.1 }

        $outBmp.SetPixel($x, $y, $color)
    }
}

$gfx.Dispose()
$outBmp.Save((Get-Item $outPath).FullName, [System.Drawing.Imaging.ImageFormat]::Png)
$outBmp.Dispose()
$srcBmp.Dispose()

Write-Output "Done: $((Get-Item $outPath).Length) bytes at $outPath"
