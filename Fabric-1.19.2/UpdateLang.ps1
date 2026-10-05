# 必须设置为 UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$LANG_DIR = "src\main\resources\assets\block_id\lang"
if (-not (Test-Path $LANG_DIR)) {
    New-Item -ItemType Directory -Path $LANG_DIR -Force | Out-Null
}

function Write-JsonFile($filename, $content) {
    $path = Join-Path $LANG_DIR $filename
    [System.IO.File]::WriteAllText($path, $content, [System.Text.Encoding]::UTF8)
    Write-Host "Updated $filename"
}

Write-JsonFile "en_us.json" @'
{
    "key.block_id.open_gui": "Open Block ID GUI",
    "key.block_id.copy_id": "Copy Block ID",
    "key.categories.block_id": "Block ID",
    "gui.block_id.title": "Block ID Manager",
    "gui.block_id.search": "Search...",
    "gui.block_id.switch_to_copy": "Switch to Copy Mode",
    "gui.block_id.switch_to_replace": "Switch to Replace Mode",
    "gui.block_id.clear_selection": "Clear Selection",
    "gui.block_id.please_select": "Please select blocks first!",
    "gui.block_id.copied_message": "【Block_ID】Copied to clipboard! Press T to paste and send.",
    "gui.block_id.source_label": "Source (Replace)",
    "gui.block_id.target_label": "Target (Replace with)",
    "gui.block_id.copy_mode_label": "Multi-select (Copy/Set)",
    "gui.block_id.selected_target": "Selected Source/Target",
    "gui.block_id.selected": "Selected",
    "gui.block_id.selected_count": "Selected count: %s",
    "gui.block_id.set": "Copy (Set)",
    "gui.block_id.copy_replace": "Copy (Replace)"
}
'@

Write-JsonFile "zh_cn.json" @'
{
    "key.block_id.open_gui": "打开方块ID管理界面",
    "key.block_id.copy_id": "复制方块ID",
    "key.categories.block_id": "方块ID",
    "gui.block_id.title": "方块ID管理器",
    "gui.block_id.search": "搜索...",
    "gui.block_id.switch_to_copy": "切换到复制模式",
    "gui.block_id.switch_to_replace": "切换到替换模式",
    "gui.block_id.clear_selection": "清空选择",
    "gui.block_id.please_select": "请先选择方块！",
    "gui.block_id.copied_message": "【Block_ID】指令已复制到剪贴板！请按 T 键粘贴发送。",
    "gui.block_id.source_label": "源方块 (替换)",
    "gui.block_id.target_label": "目标方块 (替换为)",
    "gui.block_id.copy_mode_label": "多选方块 (复制/Set)",
    "gui.block_id.selected_target": "已选源/目标",
    "gui.block_id.selected": "已选中",
    "gui.block_id.selected_count": "已选方块数量: %s",
    "gui.block_id.set": "复制 (Set)",
    "gui.block_id.copy_replace": "复制 (Replace)"
}
'@

Write-JsonFile "zh_tw.json" @'
{
    "key.block_id.open_gui": "開啟方塊ID管理介面",
    "key.block_id.copy_id": "複製方塊ID",
    "key.categories.block_id": "方塊ID",
    "gui.block_id.title": "方塊ID管理器",
    "gui.block_id.search": "搜尋...",
    "gui.block_id.switch_to_copy": "切換到複製模式",
    "gui.block_id.switch_to_replace": "切換到替換模式",
    "gui.block_id.clear_selection": "清空選擇",
    "gui.block_id.please_select": "請先選擇方塊！",
    "gui.block_id.copied_message": "【Block_ID】指令已複製到剪貼簿！請按 T 鍵貼上發送。",
    "gui.block_id.source_label": "源方塊 (替換)",
    "gui.block_id.target_label": "目標方塊 (替換為)",
    "gui.block_id.copy_mode_label": "多選方塊 (複製/Set)",
    "gui.block_id.selected_target": "已選源/目標",
    "gui.block_id.selected": "已選中",
    "gui.block_id.selected_count": "已選方塊數量: %s",
    "gui.block_id.set": "複製 (Set)",
    "gui.block_id.copy_replace": "複製 (Replace)"
}
'@

Write-JsonFile "de_de.json" @'
{
    "key.block_id.open_gui": "Block-ID-Verwaltung öffnen",
    "key.block_id.copy_id": "Block-ID kopieren",
    "key.categories.block_id": "Block-ID",
    "gui.block_id.title": "Block-ID-Manager",
    "gui.block_id.search": "Suchen...",
    "gui.block_id.switch_to_copy": "Zum Kopiermodus wechseln",
    "gui.block_id.switch_to_replace": "Zum Ersetzungsmodus wechseln",
    "gui.block_id.clear_selection": "Auswahl löschen",
    "gui.block_id.please_select": "Bitte zuerst Blöcke auswählen!",
    "gui.block_id.copied_message": "【Block_ID】Befehl kopiert! Drücken Sie T zum Einfügen und Senden.",
    "gui.block_id.source_label": "Quelle (Ersetzen)",
    "gui.block_id.target_label": "Ziel (Ersetzen durch)",
    "gui.block_id.copy_mode_label": "Mehrfachauswahl (Kopieren/Set)",
    "gui.block_id.selected_target": "Ausgewählte Quelle/Ziel",
    "gui.block_id.selected": "Ausgewählt",
    "gui.block_id.selected_count": "Ausgewählte Anzahl: %s",
    "gui.block_id.set": "Kopieren (Set)",
    "gui.block_id.copy_replace": "Kopieren (Replace)"
}
'@

Write-JsonFile "es_mx.json" @'
{
    "key.block_id.open_gui": "Abrir interfaz de gestión de ID de bloques",
    "key.block_id.copy_id": "Copiar ID de bloque",
    "key.categories.block_id": "ID de bloque",
    "gui.block_id.title": "Gestor de ID de bloques",
    "gui.block_id.search": "Buscar...",
    "gui.block_id.switch_to_copy": "Cambiar a Modo Copiar",
    "gui.block_id.switch_to_replace": "Cambiar a Modo Reemplazar",
    "gui.block_id.clear_selection": "Limpiar Selección",
    "gui.block_id.please_select": "¡Selecciona los bloques primero!",
    "gui.block_id.copied_message": "【Block_ID】¡Comando copiado! Presiona T para pegar y enviar.",
    "gui.block_id.source_label": "Fuente (Reemplazar)",
    "gui.block_id.target_label": "Destino (Reemplazar por)",
    "gui.block_id.copy_mode_label": "Selección múltiple (Copiar/Set)",
    "gui.block_id.selected_target": "Fuente/Destino seleccionados",
    "gui.block_id.selected": "Seleccionado",
    "gui.block_id.selected_count": "Cantidad seleccionada: %s",
    "gui.block_id.set": "Copiar (Set)",
    "gui.block_id.copy_replace": "Copiar (Replace)"
}
'@

Write-JsonFile "fr_fr.json" @'
{
    "key.block_id.open_gui": "Ouvrir l'interface de gestion des ID de blocs",
    "key.block_id.copy_id": "Copier l'ID du bloc",
    "key.categories.block_id": "ID de bloc",
    "gui.block_id.title": "Gestionnaire d'ID de blocs",
    "gui.block_id.search": "Rechercher...",
    "gui.block_id.switch_to_copy": "Passer en mode Copie",
    "gui.block_id.switch_to_replace": "Passer en mode Remplacement",
    "gui.block_id.clear_selection": "Effacer la sélection",
    "gui.block_id.please_select": "Veuillez d'abord sélectionner des blocs !",
    "gui.block_id.copied_message": "【Block_ID】Commande copiée ! Appuyez sur T pour coller et envoyer.",
    "gui.block_id.source_label": "Source (Remplacement)",
    "gui.block_id.target_label": "Cible (Remplacer par)",
    "gui.block_id.copy_mode_label": "Sélection multiple (Copier/Set)",
    "gui.block_id.selected_target": "Source/Cible sélectionnées",
    "gui.block_id.selected": "Sélectionné",
    "gui.block_id.selected_count": "Nombre sélectionné : %s",
    "gui.block_id.set": "Copier (Set)",
    "gui.block_id.copy_replace": "Copier (Replace)"
}
'@

Write-JsonFile "ja_jp.json" @'
{
    "key.block_id.open_gui": "ブロックID管理画面を開く",
    "key.block_id.copy_id": "ブロックIDをコピー",
    "key.categories.block_id": "ブロックID",
    "gui.block_id.title": "ブロックIDマネージャー",
    "gui.block_id.search": "検索...",
    "gui.block_id.switch_to_copy": "コピーモードに切り替え",
    "gui.block_id.switch_to_replace": "置換モードに切り替え",
    "gui.block_id.clear_selection": "選択をクリア",
    "gui.block_id.please_select": "先にブロックを選択してください！",
    "gui.block_id.copied_message": "【Block_ID】コマンドをコピーしました！Tキーで貼り付けて送信してください。",
    "gui.block_id.source_label": "ソース (置換)",
    "gui.block_id.target_label": "ターゲット (置換先)",
    "gui.block_id.copy_mode_label": "複数選択 (コピー/Set)",
    "gui.block_id.selected_target": "選択したソース/ターゲット",
    "gui.block_id.selected": "選択中",
    "gui.block_id.selected_count": "選択数: %s",
    "gui.block_id.set": "コピー (Set)",
    "gui.block_id.copy_replace": "コピー (Replace)"
}
'@

Write-JsonFile "pt_br.json" @'
{
    "key.block_id.open_gui": "Abrir interface de gerenciamento de IDs de blocos",
    "key.block_id.copy_id": "Copiar ID do bloco",
    "key.categories.block_id": "ID do bloco",
    "gui.block_id.title": "Gerenciador de IDs de blocos",
    "gui.block_id.search": "Pesquisar...",
    "gui.block_id.switch_to_copy": "Mudar para Modo Copiar",
    "gui.block_id.switch_to_replace": "Mudar para Modo Substituir",
    "gui.block_id.clear_selection": "Limpar Seleção",
    "gui.block_id.please_select": "Selecione os blocos primeiro!",
    "gui.block_id.copied_message": "【Block_ID】Comando copiado! Pressione T para colar e enviar.",
    "gui.block_id.source_label": "Fonte (Substituir)",
    "gui.block_id.target_label": "Destino (Substituir por)",
    "gui.block_id.copy_mode_label": "Seleção múltipla (Copiar/Set)",
    "gui.block_id.selected_target": "Fonte/Destino selecionados",
    "gui.block_id.selected": "Selecionado",
    "gui.block_id.selected_count": "Quantidade selecionada: %s",
    "gui.block_id.set": "Copiar (Set)",
    "gui.block_id.copy_replace": "Copiar (Replace)"
}
'@

Write-JsonFile "ru_ru.json" @'
{
    "key.block_id.open_gui": "Открыть интерфейс управления ID блоков",
    "key.block_id.copy_id": "Скопировать ID блока",
    "key.categories.block_id": "ID блока",
    "gui.block_id.title": "Менеджер ID блоков",
    "gui.block_id.search": "Поиск...",
    "gui.block_id.switch_to_copy": "Переключиться в режим копирования",
    "gui.block_id.switch_to_replace": "Переключиться в режим замены",
    "gui.block_id.clear_selection": "Очистить выбор",
    "gui.block_id.please_select": "Сначала выберите блоки!",
    "gui.block_id.copied_message": "【Block_ID】Команда скопирована! Нажмите T для вставки и отправки.",
    "gui.block_id.source_label": "Источник (Заменить)",
    "gui.block_id.target_label": "Цель (Заменить на)",
    "gui.block_id.copy_mode_label": "Множественный выбор (Копировать/Set)",
    "gui.block_id.selected_target": "Выбранные источник/цель",
    "gui.block_id.selected": "Выбрано",
    "gui.block_id.selected_count": "Выбрано: %s",
    "gui.block_id.set": "Копировать (Set)",
    "gui.block_id.copy_replace": "Копировать (Replace)"
}
'@

Write-Host ""
Write-Host "All 9 translations updated successfully!" -ForegroundColor Green