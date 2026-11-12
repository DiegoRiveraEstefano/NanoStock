@param String message
@param String type = "is-info"
@param boolean dismissible = true
@param String icon = ""

<div class="notification ${type} is-light">
    @if(dismissible)
        <button class="delete" aria-label="close"></button>
    @endif
    <div class="is-flex is-align-items-center">
        @if(!icon.isEmpty())
            <span class="mr-2 is-size-5">${icon}</span>
        @endif
        <div>${message}</div>
    </div>
</div>
