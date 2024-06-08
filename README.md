# Класс CrptApi
CrptApi(TimeUnit timeUnit, int requestLimit): Конструктор класса принимает TimeUnit и requestLimit.
## метод waitIfLimitExceeded()
Метод проверяет, не превышен ли лимит запросов, и блокируется, если лимит превышен.
## метод createDocument(Object document, String signature)
Метод создает и отправляет HTTP POST запрос с использованием HttpClient. Он также проверяет лимит запросов перед отправкой запроса и блокируется, если лимит превышен.
