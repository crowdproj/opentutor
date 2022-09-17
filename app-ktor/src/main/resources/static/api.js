/*!
 * A js-script library that contains client-api calls
 */

let keycloak

const getAllDictionariesURL = '/v1/api/dictionaries/get-all'
const getAllCardsURL = '/v1/api/cards/get-all'
const searchCardsURL = '/v1/api/cards/search'
const createCardURL = '/v1/api/cards/create'
const updateCardURL = '/v1/api/cards/update'

const getAllDictionariesRequestType = 'getAllDictionaries'
const getAllCardsRequestType = 'getAllCards'
const searchCardsRequestType = 'searchCards'
const createCardRequestType = 'createCard'
const updateCardRequestType = 'updateCard'

async function initKeycloak() {
    if (devMode) {
        keycloak = null
        return
    }
    const res = new Keycloak({
        url: keycloakAuthURL,
        realm: keycloakAppRealm,
        clientId: keycloakAppClient,
    });
    await res.init({
        onLoad: 'check-sso',
    }).catch(function (error) {
        throw new Error('keycloak-init-error::' + error)
    });
    keycloak = res
}

function getDictionaries(onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': getAllDictionariesRequestType,
        'debug': {'mode': runMode}
    }
    post(getAllDictionariesURL, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            onDone(res.dictionaries)
        }
    })
}

function getCards(dictionaryId, onDone) {
    const data = {
        'dictionaryId': dictionaryId,
        'requestId': uuid(),
        'requestType': getAllCardsRequestType,
        'debug': {'mode': runMode}
    }
    post(getAllCardsURL, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            onDone(res.cards)
        }
    })
}

function getNextCardDeck(dictionaryId, length, onDone) {
    if (length == null) {
        length = numberOfWordsToShow
    }
    const data = {
        'dictionaryIds': [dictionaryId],
        'requestId': uuid(),
        'requestType': searchCardsRequestType,
        'random': true,
        'length': length,
        'debug': {'mode': runMode}
    }
    post(searchCardsURL, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            onDone(res.cards)
        }
    })
}

function uploadDictionary(data, onDone, onFail) {
    // TODO
    console.log("uploadDictionary")
}

function downloadDictionaryURL(id) {
    // TODO
    console.log("downloadDictionaryURL")
    return ""
}

function deleteDictionary(id, onDone) {
    // TODO
    console.log("deleteDictionary")
}

function createCard(card, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': createCardRequestType,
        'card' : card,
        'debug': {'mode': runMode},
    }
    post(createCardURL, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            onDone(res.card.cardId)
        }
    })
}

function updateCard(card, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': updateCardRequestType,
        'card' : card,
        'debug': {'mode': runMode},
    }
    post(updateCardURL, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            onDone(res.card.cardId)
        }
    })
}

function deleteCard(id, onDone) {
    // TODO
    console.log("deleteCard")
}

function resetCard(id, onDone) {
    // TODO
    console.log("resetCard")
}

function learnCard(cards, onDone) {
    // TODO
    console.log("learnCard")
    onDone()
}

function playAudio(resource, callback) {
    // TODO
    console.log("playAudio")
}

function hasResponseErrors(res) {
    return res.errors !== undefined && res.errors.length !== 0
}

function handleResponseErrors(res) {
    if (devMode) {
        console.log(res.errors)
    }
}

function post(url, requestData, onDone, onFail) {
    if (onFail === undefined) {
        onFail = function (error) {
            if (devMode) {
                console.log(error)
            }
        }
    }
    if (devMode) {
        $.ajax({
            type: 'POST',
            url: url,
            contentType: 'application/json',
            data: JSON.stringify(requestData),
        }).done(onDone).fail(onFail);
    } else {
        authPost(url, requestData, onDone, onFail)
    }
}

function authPost(url, requestData, onDone, onFail, runAgain) {
    if (runAgain === undefined) {
        runAgain = true
    }
    // noinspection JSUnusedLocalSymbols
    $.ajax({
        type: 'POST',
        url: url,
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        headers: {'Authorization': 'Bearer ' + keycloak.token}
    }).done(onDone).fail(function (jqXHR, status, error) {
        const code = jqXHR.status
        if (code === 401 && runAgain) {
            // try again
            keycloak.updateToken().then(function () {
                authPost(url, requestData, onDone, onFail, false)
            })
        } else {
            onFail(error)
        }
    });
}

function logout() {
    if (devMode) {
        return
    }
    keycloak.logout().catch(function (error) {
        throw new Error('logout-error::' + error)
    });
}