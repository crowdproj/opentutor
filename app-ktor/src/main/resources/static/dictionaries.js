/*!
 * page:dictionaries js-script library.
 */

function drawDictionariesPage() {
    getDictionaries(function (response) {
        displayPage('dictionaries');

        const tbody = $('#dictionaries tbody');

        tbody.html('');
        initTableListeners('dictionaries', resetDictionarySelection);

        $('#dictionaries-btn-run').off().on('click', drawRunPage);
        $('#dictionaries-btn-edit').off().on('click', drawDictionaryPage);
        $('#dictionaries-table-row').css('height', calcInitTableHeight());
        $('#dictionaries-btn-upload').off().on('click', () => {
            $('#dictionaries-btn-upload-label').removeClass('btn-outline-danger');
        }).on('change', (e) => {
            const file = e.target.files[0];
            if (file !== undefined) {
                uploadDictionaryFile(file);
            }
        });
        $('#dictionaries-btn-download').off().on('click', downloadDictionaryFile);

        bootstrap.Modal.getOrCreateInstance(document.getElementById('delete-dictionary-prompt')).hide();
        initDictionaryDeletePrompt();

        $.each(response, function (key, value) {
            let row = $(`<tr id="${'d' + value.dictionaryId}">
                            <td>${value.sourceLang}</td>
                            <td>${value.targetLang}</td>
                            <td>${value.name}</td>
                            <td>${value.total}</td>
                            <td>${value.learned}</td>
                          </tr>`);
            row.on('click', function () {
                dictionaryRowOnClick(row, value);
            });
            row.dblclick(drawRunPage);
            tbody.append(row);
        });
    });
}

function uploadDictionaryFile(file) {
    const btnUpload = $('#dictionaries-btn-upload-label');
    const reader = new FileReader()
    reader.onload = function (e) {
        const txt = e.target.result;
        uploadDictionary(txt, drawDictionariesPage, function () {
            btnUpload.addClass('btn-outline-danger');
        });
    }
    reader.readAsArrayBuffer(file);
    $('#dictionaries-btn-upload').val('');
}

function downloadDictionaryFile() {
    if (selectedDictionary == null) {
        return;
    }
    const tmpLink = document.createElement("a");
    tmpLink.href = downloadDictionaryURL(selectedDictionary.dictionaryId);
    tmpLink.download = toFilename(selectedDictionary.name) + '-' + new Date().toISOString().substring(0, 19) + '.xml';
    document.body.appendChild(tmpLink);
    tmpLink.click();
    setTimeout(function () {
        document.body.removeChild(tmpLink);
    }, 0);
}

function initDictionaryDeletePrompt() {
    $('#delete-dictionary-prompt-confirm').off('click').on('click', function () {
        const body = $('#delete-dictionary-prompt-body');
        const id = body.attr('item-id');
        if (!id) {
            return;
        }
        deleteDictionary(id, drawDictionariesPage);
    });
}

function dictionaryRowOnClick(row, dict) {
    selectedDictionary = dict;
    const tbody = $('#dictionaries tbody');
    const btnRun = $('#dictionaries-btn-run');
    const btnEdit = $('#dictionaries-btn-edit');
    const btnDelete = $('#dictionaries-btn-delete');
    const btnDownload = $('#dictionaries-btn-download');
    resetRowSelection(tbody);
    markRowSelected(row);
    btnRun.prop('disabled', false);
    btnEdit.prop('disabled', false);
    btnDelete.prop('disabled', false);
    btnDownload.prop('disabled', false);

    const body = $('#delete-dictionary-prompt-body');
    body.attr('item-id', dict.dictionaryId);
    body.html(dict.name);
}

function resetDictionarySelection() {
    selectedDictionary = null;
    $('#dictionaries-btn-group button').each(function (i, b) {
        $(b).prop('disabled', true);
    });
    $('#dictionaries-btn-upload-label').removeClass('btn-outline-danger');
}

function drawRunPage() {
    if (selectedDictionary == null) {
        return;
    }
    resetRowSelection($('#dictionaries tbody'));
    getNextCardDeck(selectedDictionary.dictionaryId, null, function (array) {
        flashcards = array;
        stageShow();
    });
}