@param int currentPage
@param int totalPages
@param String baseUrl

@if(totalPages > 1)
    <nav class="pagination is-centered is-rounded is-small" role="navigation" aria-label="pagination">
        @if(currentPage > 1)
            <a href="${baseUrl}?page=${currentPage - 1}" class="pagination-previous">Anterior</a>
        @else
            <button class="pagination-previous" disabled>Anterior</button>
        @endif

        @if(currentPage < totalPages)
            <a href="${baseUrl}?page=${currentPage + 1}" class="pagination-next">Siguiente</a>
        @else
            <button class="pagination-next" disabled>Siguiente</button>
        @endif

        <ul class="pagination-list">
            @for(int i = 1; i <= totalPages; i++)
                <li>
                    <a href="${baseUrl}?page=${i}" 
                       class="pagination-link ${i == currentPage ? "is-current" : ""}" 
                       aria-label="Ir a página ${i}">
                        ${i}
                    </a>
                </li>
            @endfor
        </ul>
    </nav>
@endif
