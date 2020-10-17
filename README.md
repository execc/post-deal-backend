# Цифровой Прорыв 2020. Почта.Сделка. Бекенд и Смарт-Контракт. 

## Настройка проекта
Перед сборкой проекта, создайте файл `.gradle/gradle.properties` в домашней директории.


В файле пропишите параметры доступа к nexus (библиотеки WE).
```
mavenUser=лонин в нексус WE
mavenPassword=пароль от нексуса WE
```

## Сборка (все модули)
Команда ниже используется для сборки всех образов.

`./gradlew clean dockerTag`


## Запуск (из IDE)
Приложение уже настроено на использование тестового стенда (блокчейн узлов). 
Для запуска приложения достаточно после сборки образов выполнить комнаду:

`docker run -p 8080:8080 registry.wavesenterprise.com/deals/deals-webapp-app`

## Техническая реализация
Приложение реализовано как микросвервис на платформе Spring Boot.

Для работы с Verifiable Credentials используется библиотека 
https://github.com/METADIUM/verifiable-credential-java

Для работы с блокчейном Waves Enterprise используется SDK WE 
https://sdk.weintegrator.com

При проверки подписи использутеся кривая `sekp256k1`.
Для генерации ключевых пар и подписи без фронтенда 
можно использовать сервис https://8gwifi.org/ecsignverify.jsp

Основой для работы приложения является реализации открытых стандартов
децентрализованной идентификации (Decentralized Identity, DiD) и 
проверяемых документов (Verifiable Credential, VC). 
Информацию о стандартах можно получить на сайте w3c https://www.w3.org/TR/vc-data-model/

## Функции приложения

Приложение представляет собой сервис Доверенной третьей стороны
(верификатора), обеспечивающий: 
 - идентификацию клиентов с
использованием биометрического криптографического идентификатора
 - ведение реестра децентрализованных идентичностей в распределенном
реестре (блокчейне)
 - выпуск проверяемых документов 
(Verifiable Credentials, VC) хранящих в себе информацию о сделке
- их распространение между участниками сделки 
- заверение созданных документов в распределенном реестре
- ведение архива выпущенных документов

## Работа с API

### Создание децентрализованной идентичности (Decentralized Identity, DiD)
Вначале создадим запрос на создание личности. Здесь
 - login - желаемое имя пользователя
 - publicKey - base58(sha256(publicKey))
 - biometricPublicKey - публичный ключ ключевой пары, закрытой биометрическим паролем
 - password - желаемый пароль
 - phoneNumber - номер телефона для проверки
```
curl --location --request POST 'http://deals.weintegrator.com/api/v0/deals-webapp-app/public/did/issue' \
--header 'Content-Type: application/json' \
--data-raw '{
    "login": "igork",
    "publicKey": "3FqDhAXdEYyc49roUhVqgR8ZRqCtwf4eE1U",
    "biometricPublicKey": "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEbOsWbd7vgXvr8I/KdftMx3Y4Ye+IfEnG0Qs8S2ALjl91oKbzTahHnV12H6iGPe+wj8l2c6tKq0gRw1uK5jA+Iw==",
    "password": "password",
    "phoneNumber": "+79253468737",
    "firstName": "Игорь",
    "patronymic": "",
    "lastName": "Кузьмичев"
}'
```

В ответ получим
```
{
    "status": "NEW",
    "challenge": "7dbfb705-e8c5-43a7-bb20-5081e3029d55"
}
```

Необходимо подтвердить:
 - владение закрытой частью публичного ключа
 - владение телефоном
 
Подтверждаем УЗ запросом
 - activationCode - код из СМС
 - signedChallenge - подпись строки challenge из ответа на создание
```
curl --location --request POST 'http://deals.weintegrator.com/api/v0/deals-webapp-app/public/did/activate' \
--header 'Content-Type: application/json' \
--data-raw '{
    "publicKey": "3FqDhAXdEYyc49roUhVqgR8ZRqCtwf4eE1U",
    "activationCode": "4004",
    "signedChallenge": "MEUCIQC1XFyDqSSbTpUHQHC/J0397WBXPOQq3cgya6yt2ZJS/gIgZYoynlQNSoG+lA27WrjwTV1jmXoL8HVC30vZ2/tf2GI="
}'
```

В ответ получаем:
```
{
    "status": "PENDING",
    "jwt": "eyJraWQiOiJkaWQ6d2U6M0ZxRGhBWGRFWXljNDlyb1VoVnFnUjhaUnFDdHdmNGVFMVUjb3duZXIiLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJkaWQ6d2U6M0ZxRGhBWGRFWXljNDlyb1VoVnFnUjhaUnFDdHdmNGVFMVUiLCJleHAiOjE2MzQ0MTgwMDAsImlhdCI6MTYwMjg4MjAwMCwibm9uY2UiOiIyYzc1NTAxNC00Nzg0LTRmZmYtYTRkMC04NmY0MmNjZGUxYTQiLCJ2YyI6eyJAY29udGV4dCI6WyJodHRwczpcL1wvdzNpZC5vcmdcL2NyZWRlbnRpYWxzXC92MSJdLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiSWRlbnRpdHkiXSwiY3JlZGVudGlhbFN1YmplY3QiOnsiaWQiOiIzRnFEaEFYZEVZeWM0OXJvVWhWcWdSOFpScUN0d2Y0ZUUxVSIsIm5hbWUiOiLQmNCz0L7RgNGMICDQmtGD0LfRjNC80LjRh9C10LIiLCJwaG9uZU51bWJlciI6Iis3OTI1MzQ2ODczNyIsImJpcnRoRGF5RGF0ZSI6IjEwXC8xXC8xOTg0IiwiZ2VuZGVyIjoiTSIsImJpcnRoUGxhY2UiOiLQnNC-0YHQutCy0LAiLCJwYXNzcG9ydFNlcmlhbCI6IjIxOTQiLCJwYXNzcG9ydE51bWJlciI6IjI3MDk4NyIsInBhc3Nwb3J0SXNzdWVyIjoi0J7QktCUINC_0L4g0KDQsNC50L7QvdGDINCR0L7Qs9C-0YDQvtC00YHQutC-0LUiLCJwYXNzcG9ydElzc3VlckNvZGUiOiI0NTMzIiwidHhJZCI6IkMxbUdoSEpzNXJNUXpqM3MydVgzYkhZSFJqMnU0dVZFMVkyU2VOSHBGN2NuIn19LCJqdGkiOiJodHRwczpcL1wvZGVhbHMud2VpbnRlZ3JhdG9yLmNvbVwvYXBpXC92MFwvZGVhbHMtd2ViYXBwLWFwcFwvcHVibGljXC92Y1wvQzFtR2hISnM1ck1RemozczJ1WDNiSFlIUmoydTR1VkUxWTJTZU5IcEY3Y24ifQ.dSZCff2ltRxiIc7-KO-hCid0d24imc7cVcXCPJz2cqW5Ua-yXn6H7SUKGPtet5Gyj4gi2byWoLWmR_6bXJyUOA",
    "payload": {
        "iss": "did:we:3FqDhAXdEYyc49roUhVqgR8ZRqCtwf4eE1U",
        "exp": 1634418000,
        "iat": 1602882000,
        "nonce": "2c755014-4784-4fff-a4d0-86f42ccde1a4",
        "vc": {
            "@context": [
                "https://w3id.org/credentials/v1"
            ],
            "type": [
                "VerifiableCredential",
                "Identity"
            ],
            "credentialSubject": {
                "id": "3FqDhAXdEYyc49roUhVqgR8ZRqCtwf4eE1U",
                "name": "Игорь  Кузьмичев",
                "phoneNumber": "+79253468737",
                "birthDayDate": "10/1/1984",
                "gender": "M",
                "birthPlace": "Москва",
                "passportSerial": "2194",
                "passportNumber": "270987",
                "passportIssuer": "ОВД по Району Богородское",
                "passportIssuerCode": "4533",
                "txId": "C1mGhHJs5rMQzj3s2uX3bHYHRj2u4uVE1Y2SeNHpF7cn"
            }
        },
        "jti": "https://deals.weintegrator.com/api/v0/deals-webapp-app/public/vc/C1mGhHJs5rMQzj3s2uX3bHYHRj2u4uVE1Y2SeNHpF7cn"
    }
}
```
- status - статус записи в блокчейн (в процессе)
- jwt - подписанный Verifiable Credential, удостоверяющий личность
- payload - декодированный JWT

Поле txId содержит в себе идентификатор блокчейн транзакции внесения 
децентрализованной идентичности в распределенный реестр. 

Пример фиксации информации о создании децентрализованной 
идентичности можно посмотреть в блокчейн эксплорере по адресу
https://explorer.deals.weintegrator.com/explorer/transactions/id/Fm1PsPjA9nUhfAtFLfs8yt6PfsfW1CpxfdGChLseUk7A/data

Или на примере (скриншот)
![DiD in Blockchain](/src_bc_1.png)
