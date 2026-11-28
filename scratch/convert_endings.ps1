Add-Type -AssemblyName System.Drawing

$brainDir = "$env:USERPROFILE\.gemini\antigravity\brain\cfc42039-c0da-4077-af8f-9d39ac7b6b8f"
$map = @{
    "ending_dawn_victory_1795792095170.jpg" = "assets/img/ending_dawn_victory.png"
    "ending_warden_concession_1795792104471.jpg" = "assets/img/ending_warden_concession.png"
    "ending_cat_victory_1795792179123.jpg" = "assets/img/ending_cat_victory.png"
    "ending_turtle_victory_1795792124231.jpg" = "assets/img/ending_turtle_victory.png"
    "ending_fox_victory_1795792133704.jpg" = "assets/img/ending_fox_victory.png"
    "ending_raccoon_victory_1795792143339.jpg" = "assets/img/ending_raccoon_victory.png"
    "ending_fellowship_victory_1795792152956.jpg" = "assets/img/ending_fellowship_victory.png"
}

foreach ($entry in $map.GetEnumerator()) {
    $src = Join-Path $brainDir $entry.Key
    $dst = $entry.Value
    if (Test-Path $src) {
        $img = [System.Drawing.Image]::FromFile($src)
        $img.Save($dst, [System.Drawing.Imaging.ImageFormat]::Png)
        $img.Dispose()
        Write-Output "Converted $src -> $dst"
    } else {
        Write-Error "Not found: $src"
    }
}
