/*!
 * main common script, contains generic functions.
 */


async function renderPage() {
    if (devMode) {
        console.log('This is dev mode')
    }
    await initKeycloak()
    drawDictionariesPage()
}

function displayPage(id) {
    if (id === 'dictionaries') {
        $('#go-home').addClass('disabled').off('click');
    } else {
        $('#go-home').removeClass('disabled').on('click', () => window.location.reload());
    }

    $.each($('.page'), function (k, v) {
        let x = $(v);
        if (x.attr('id') === id) {
            return;
        }
        $(x).hide();
    });
    $('#' + id).show()
}