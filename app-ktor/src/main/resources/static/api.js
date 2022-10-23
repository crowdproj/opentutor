/*!
 * A js-script library that contains client-api calls
 */

let keycloak

const getAllDictionariesURI = '/v1/api/dictionaries/get-all'
const deleteDictionaryURI = '/v1/api/dictionaries/delete'
const getAllCardsURI = '/v1/api/cards/get-all'
const searchCardsURI = '/v1/api/cards/search'
const createCardURI = '/v1/api/cards/create'
const updateCardURI = '/v1/api/cards/update'
const resetCardURI = '/v1/api/cards/reset'
const deleteCardURI = '/v1/api/cards/delete'
const getAudioURI = '/v1/api/sounds/get'

const getAllDictionariesRequestType = 'getAllDictionaries'
const deleteDictionaryRequestType = 'deleteDictionary'
const getAllCardsRequestType = 'getAllCards'
const searchCardsRequestType = 'searchCards'
const createCardRequestType = 'createCard'
const updateCardRequestType = 'updateCard'
const resetCardRequestType = 'resetCard'
const deleteCardRequestType = 'deleteCard'
const getAudioRequestType = 'getAudio'

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
    post(getAllDictionariesURI, data, function (res) {
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
    post(getAllCardsURI, data, function (res) {
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
    post(searchCardsURI, data, function (res) {
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

function deleteDictionary(dictionaryId, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': deleteDictionaryRequestType,
        'dictionaryId': dictionaryId,
        'debug': {'mode': runMode},
    }
    post(deleteDictionaryURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            onDone()
        }
    })
}

function createCard(card, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': createCardRequestType,
        'card': card,
        'debug': {'mode': runMode},
    }
    post(createCardURI, data, function (res) {
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
        'card': card,
        'debug': {'mode': runMode},
    }
    post(updateCardURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            onDone(res.card.cardId)
        }
    })
}

function deleteCard(cardId, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': deleteCardRequestType,
        'cardId': cardId,
        'debug': {'mode': runMode},
    }
    post(deleteCardURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            onDone()
        }
    })
}

function resetCard(cardId, onDone) {
    const data = {
        'requestId': uuid(),
        'requestType': resetCardRequestType,
        'cardId': cardId,
        'debug': {'mode': runMode},
    }
    post(resetCardURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            onDone()
        }
    })
}

function learnCard(cards, onDone) {
    // TODO
    console.log("learnCard")
    onDone()
}

function playAudio(resourcePath, callback) {
    if (!callback) {
        callback = () => {
        };
    }
    const path = resourcePath.split(":")
    const data = {
        'requestId': uuid(),
        'requestType': getAudioRequestType,
        'lang': path[0],
        'word': path[1],
        'debug': {'mode': runMode},
    }
    post(getAudioURI, data, function (res) {
        if (hasResponseErrors(res)) {
            handleResponseErrors(res)
        } else {
            const bytes = base64StringToUint8Array(res.resource).buffer
            const blob = new Blob([bytes], {type: 'audio/wav'});
            const url = window.URL.createObjectURL(blob);
            new Audio(url).play().then(callback);
        }
    })
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