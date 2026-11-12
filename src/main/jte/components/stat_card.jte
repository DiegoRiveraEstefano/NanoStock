@param String title
@param String value
@param String icon = "📊"
@param String helperText = ""
@param String colorClass = "has-text-primary"
@param String link = ""

<div class="nano-stat-card">
    <div class="is-flex is-justify-content-space-between is-align-items-center mb-1">
        <span class="stat-label">${title}</span>
        <span class="is-size-4">${icon}</span>
    </div>
    <div class="stat-value ${colorClass}">${value}</div>
    @if(!helperText.isEmpty())
        <div class="is-size-7 has-text-grey">${helperText}</div>
    @endif
    @if(!link.isEmpty())
        <a href="${link}" class="is-size-7 has-text-link mt-2 is-inline-block">Ver más →</a>
    @endif
</div>
