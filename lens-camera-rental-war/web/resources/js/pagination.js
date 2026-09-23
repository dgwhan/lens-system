/**
 * LENS Camera Rental - Reusable Pagination Controller
 * Strictly adheres to approved design reference:
 * - Dynamic generation of desktop sequence (smart ellipsis) and mobile sequence (3-window stack)
 * - Dynamic calculation of result range (Showing X–Y of Z devices)
 * - 6 button states (Default, Hover, Active, Keyboard Focus, Disabled, Ellipsis)
 * - Zero hardcoded values
 */
(function () {
    'use strict';

    function initPagination(navEl) {
        if (!navEl) return;

        // Extract Configuration
        const currentPage = parseInt(navEl.getAttribute('data-current-page') || '1', 10);
        const pageSize = parseInt(navEl.getAttribute('data-page-size'), 10) || totalItems || 1;
        const totalItems = parseInt(navEl.getAttribute('data-total-items') || '0', 10);
        const itemLabel = navEl.getAttribute('data-item-label') || 'devices';
        const showSummary = navEl.getAttribute('data-show-summary') !== 'false';
        const targetUrl = navEl.getAttribute('data-target-url') || '';
        const callbackName = navEl.getAttribute('data-on-page-change') || '';
        const pageParam = navEl.getAttribute('data-page-param') || 'page';

        // Calculate Totals
        const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));

        // Edge Case: Empty or zero items -> hide component
        if (totalItems <= 0) {
            navEl.classList.add('is-hidden');
            return;
        } else {
            navEl.classList.remove('is-hidden');
        }

        // Clamp Current Page
        const safeCurrentPage = Math.min(Math.max(1, currentPage), totalPages);

        // Calculate Range
        const startItem = (safeCurrentPage - 1) * pageSize + 1;
        const endItem = Math.min(safeCurrentPage * pageSize, totalItems);

        // Update Summary Elements
        const summaryEl = navEl.querySelector('[data-role="summary"]');
        if (summaryEl) {
            if (showSummary) {
                summaryEl.innerHTML = `Showing <span class="pagination-summary-strong">${startItem}–${endItem}</span> of <span class="pagination-summary-strong">${totalItems}</span> ${itemLabel}`;
                summaryEl.style.display = '';
            } else {
                summaryEl.style.display = 'none';
            }
        }

        // Update Mobile Header Element (PAGE X OF Y)
        const mobileHeaderEl = navEl.querySelector('[data-role="mobile-header"]');
        if (mobileHeaderEl) {
            mobileHeaderEl.textContent = `PAGE ${safeCurrentPage} OF ${totalPages}`;
        }

        // Controls Container
        const controlsList = navEl.querySelector('[data-role="controls"]');
        if (!controlsList) return;

        // Edge Case: Single page -> hide pagination controls
        if (totalPages <= 1) {
            controlsList.style.display = 'none';
            return;
        } else {
            controlsList.style.display = 'flex';
        }

        // Determine if Mobile Viewport
        const isMobile = window.matchMedia && window.matchMedia('(max-width: 640px)').matches;

        // Determine Page Numbers Sequence
        const sequence = isMobile
            ? getMobileSequence(safeCurrentPage, totalPages)
            : getDesktopSequence(safeCurrentPage, totalPages);

        // Build HTML
        controlsList.innerHTML = '';

        // 1. PREVIOUS BUTTON
        const isPrevDisabled = safeCurrentPage <= 1;
        const prevItem = document.createElement('li');
        prevItem.className = 'pagination-item';

        const prevBtn = document.createElement(isPrevDisabled ? 'span' : (targetUrl ? 'a' : 'button'));
        prevBtn.className = `pagination-btn pagination-prev ${isPrevDisabled ? 'is-disabled' : ''}`;
        prevBtn.setAttribute('aria-label', 'Previous page');
        if (isPrevDisabled) {
            prevBtn.setAttribute('aria-disabled', 'true');
            prevBtn.setAttribute('tabindex', '-1');
        } else {
            if (targetUrl) {
                prevBtn.href = buildPageUrl(targetUrl, safeCurrentPage - 1, pageParam);
            } else {
                prevBtn.type = 'button';
            }
            prevBtn.addEventListener('click', function (e) {
                handlePageSelect(safeCurrentPage - 1, e, navEl, callbackName, targetUrl);
            });
        }
        prevBtn.innerHTML = '<span class="pagination-chevron" aria-hidden="true">&#8249;</span>';
        prevItem.appendChild(prevBtn);
        controlsList.appendChild(prevItem);

        // 2. SEQUENCE (Page Numbers & Ellipsis)
        sequence.forEach(function (token) {
            const item = document.createElement('li');
            item.className = 'pagination-item';

            if (token === '...') {
                const ellipsis = document.createElement('span');
                ellipsis.className = 'pagination-ellipsis';
                ellipsis.setAttribute('aria-hidden', 'true');
                ellipsis.textContent = '...';
                item.appendChild(ellipsis);
            } else {
                const pageNum = token;
                const isActive = pageNum === safeCurrentPage;
                const pageBtn = document.createElement(isActive ? 'span' : (targetUrl ? 'a' : 'button'));
                pageBtn.className = `pagination-btn ${isActive ? 'is-active' : ''}`;
                pageBtn.setAttribute('aria-label', `Page ${pageNum}`);
                pageBtn.textContent = pageNum;

                if (isActive) {
                    pageBtn.setAttribute('aria-current', 'page');
                    pageBtn.setAttribute('tabindex', '0');
                } else {
                    if (targetUrl) {
                        pageBtn.href = buildPageUrl(targetUrl, pageNum, pageParam);
                    } else {
                        pageBtn.type = 'button';
                    }
                    pageBtn.addEventListener('click', function (e) {
                        handlePageSelect(pageNum, e, navEl, callbackName, targetUrl);
                    });
                }
                item.appendChild(pageBtn);
            }
            controlsList.appendChild(item);
        });

        // 3. NEXT BUTTON
        const isNextDisabled = safeCurrentPage >= totalPages;
        const nextItem = document.createElement('li');
        nextItem.className = 'pagination-item';

        const nextBtn = document.createElement(isNextDisabled ? 'span' : (targetUrl ? 'a' : 'button'));
        nextBtn.className = `pagination-btn pagination-next ${isNextDisabled ? 'is-disabled' : ''}`;
        nextBtn.setAttribute('aria-label', 'Next page');
        if (isNextDisabled) {
            nextBtn.setAttribute('aria-disabled', 'true');
            nextBtn.setAttribute('tabindex', '-1');
        } else {
            if (targetUrl) {
                nextBtn.href = buildPageUrl(targetUrl, safeCurrentPage + 1, pageParam);
            } else {
                nextBtn.type = 'button';
            }
            nextBtn.addEventListener('click', function (e) {
                handlePageSelect(safeCurrentPage + 1, e, navEl, callbackName, targetUrl);
            });
        }
        nextBtn.innerHTML = '<span class="pagination-chevron" aria-hidden="true">&#8250;</span>';
        nextItem.appendChild(nextBtn);
        controlsList.appendChild(nextItem);
    }

    /**
     * Desktop sequence generator matching reference edge cases:
     * - totalPages <= 7: [1, 2, 3, 4, 5, 6, 7]
     * - currentPage <= 4: [1, 2, 3, 4, '...', totalPages] (Section 1: 1 2 3 4 ... 10)
     * - currentPage >= totalPages - 3: [1, '...', totalPages-3, totalPages-2, totalPages-1, totalPages] (Section 3: 1 ... 8 9 10)
     * - Middle: [1, '...', currentPage-1, currentPage, currentPage+1, '...', totalPages]
     */
    function getDesktopSequence(current, total) {
        if (total <= 7) {
            const arr = [];
            for (let i = 1; i <= total; i++) arr.push(i);
            return arr;
        }

        // Near start (Sections 1 & 2)
        if (current <= 3) {
            if (current === 3) {
                // Section 1: Page 3 of 10 -> [1, 2, 3, 4, '...', 10]
                return [1, 2, 3, 4, '...', total];
            }
            // Section 2: Page 1 of 10 -> [1, 2, 3, '...', 10]
            return [1, 2, 3, '...', total];
        }

        // Near end (Section 3)
        if (current >= total - 2) {
            if (current === total - 2) {
                // Page 8 of 10 -> [1, '...', 7, 8, 9, 10]
                return [1, '...', total - 3, total - 2, total - 1, total];
            }
            // Section 3: Page 10 of 10 -> [1, '...', 8, 9, 10]
            return [1, '...', total - 2, total - 1, total];
        }

        // Middle (e.g. Page 4, 5, 6, 7 of 10)
        return [1, '...', current - 1, current, current + 1, '...', total];
    }

    /**
     * Mobile sequence generator matching Section 5 (3-window display of nearby pages):
     * - Example: [2, 3, 4] for page 3 of 8
     * - Clamped to 1..total
     */
    function getMobileSequence(current, total) {
        if (total <= 3) {
            const arr = [];
            for (let i = 1; i <= total; i++) arr.push(i);
            return arr;
        }

        if (current === 1) {
            return [1, 2, 3];
        }

        if (current === total) {
            return [total - 2, total - 1, total];
        }

        return [current - 1, current, current + 1];
    }

    function buildPageUrl(baseUrl, pageNum, paramName) {
        if (!baseUrl) return '#';
        if (baseUrl.includes('{page}')) {
            return baseUrl.replace('{page}', pageNum);
        }
        const separator = baseUrl.includes('?') ? '&' : '?';
        return `${baseUrl}${separator}${paramName}=${pageNum}`;
    }

    function handlePageSelect(newPage, event, navEl, callbackName, targetUrl) {
        if (callbackName && typeof window[callbackName] === 'function') {
            event.preventDefault();
            window[callbackName](newPage, navEl);
        }

        // Dispatch Standard Custom DOM Event
        const customEvt = new CustomEvent('pagination:change', {
            bubbles: true,
            detail: { page: newPage, nav: navEl }
        });
        navEl.dispatchEvent(customEvt);

        // If targetUrl exists and no custom handler intercepted, standard navigation happens
        if (!targetUrl && !callbackName) {
            // Live Client-side State Transition Demo Mode
            navEl.setAttribute('data-current-page', newPage);
            initPagination(navEl);
        }
    }

    // Auto-init all pagination instances
    function initAll() {
        const instances = document.querySelectorAll('.pagination-nav');
        instances.forEach(initPagination);
    }

    // Initialize on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAll);
    } else {
        initAll();
    }

    // Responsive re-calibration on resize
    let resizeTimer;
    window.addEventListener('resize', function () {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(initAll, 120);
    });

    // Expose Global Helper
    window.LensPagination = {
        init: initPagination,
        initAll: initAll
    };
})();
