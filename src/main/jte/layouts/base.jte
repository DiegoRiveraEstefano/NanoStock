@import gg.jte.Content

@param String title = "NanoStock"
@param Content head = null
@param Content content
@param Content scripts = null

<!DOCTYPE html>
<html lang="es" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title} - NanoStock</title>
    <link rel="stylesheet" href="/css/bulma.min.css">
    <link rel="stylesheet" href="/css/app.css">
    @if(head != null)
        ${head}
    @endif
</head>
<body>
    ${content}

    <div id="nano-toast-container"></div>

    <script src="/js/app.js"></script>
    <script type="module" src="/js/components/nano-modal.js"></script>
    @if(scripts != null)
        ${scripts}
    @endif
</body>
</html>
