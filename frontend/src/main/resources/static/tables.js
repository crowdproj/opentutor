/*!
 * generic functions to work with tables.
 */

const tableHeightRation = 2. / 3;
const lgFrameHeightRation = 7. / 18;

function initTableListeners(id, onResetSelection) {
    const thead = $('#' + id + ' thead');
    const title = $('#' + id + ' .card-title');
    const tbody = $('#' + id + ' tbody');

    onResetSelection();
    thead.off('click').on('click', function () {
        resetRowSelection(tbody);
        onResetSelection();
    });
    title.off('click').on('click', function () {
        resetRowSelection(tbody);
        onResetSelection();
    });
}

function resetRowSelection(tbody) {
    $('tr', tbody).each(function (i, r) {
        $(r).removeClass();
    })
}

function scrollToRow(rowSelector, headerSelector, onScroll) {
    const start = new Date();
    const timeout = 2000;
    const wait = setInterval(function () {
        const row = $(rowSelector);
        const header = $(headerSelector)
        if (row.length && header.length) {
            const position = row.offset().top - header.offset().top + header.scrollTop();
            header.scrollTop(position);
            if (onScroll) {
                onScroll(row);
            }
            clearInterval(wait);
        } else if (new Date() - start > timeout) {
            clearInterval(wait);
        }
    }, 50);
}

function markRowSelected(row) {
    row.addClass('table-success');
}

function isRowSelected(row) {
    return row.hasClass('table-success');
}

function findSelectedRows(tbody) {
    return tbody.find('tr').filter(function () {
        return isRowSelected($(this));
    });
}

function calcInitTableHeight() {
    return Math.round($(document).height() * tableHeightRation);
}

function calcInitLgFrameHeight() {
    return Math.round($(document).height() * lgFrameHeightRation);
}