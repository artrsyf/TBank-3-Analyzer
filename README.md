# Проект 3: Анализатор логов

## Работа с проектом

Для запуска проекта нужно:
- Локальные файлы должны быть в директории `src/main/resources` (даже после компиляции);
- Запустить скрипт:
```bash
sbt run --path <path1>,<path2>,... [--from <date>] [--to <date>] [--format <format>] [--filter-field <field>] [--filter-value <value>]
```

Для получения подробной справки по параметрам нужно:

```bash
sbt run
```

Также можно транслировать исходный код в .jar файл и прогнать его через JVM:

```bash
sbt assembly
java -jar <.jar path> args...
```

# Описание параметров

`--path` (обязательный)
- Описание: Пути к логам, разделённые запятыми. Логи должны быть в кодировке UTF-8. Поддерживаются:
    - Локальные файлы с использованием glob-шаблонов (например, *.txt).
    - URL-адреса (например, http://example.com/logs.txt).
- Формат значения: `<path1>,<path2>,...`
- Пример:
```bash
--path https://example.com/nginx_logs/nginx_logs,src/main/resources/*.txt
```

`--from` (необязательный)
- Описание: Начальная дата для фильтрации логов. Логи, созданные до этой даты, исключаются из анализа.
- Формат значения: `Дата в формате ISO8601 (YYYY-MM-DDTHH:MM:SSZ)`
- Пример:
```bash
--from 2009-01-01T00:00:00Z
```

`--to` (необязательный)
- Описание: Конечная дата для фильтрации логов. Логи, созданные после этой даты, исключаются из анализа.
- Формат значения: `Дата в формате ISO8601 (YYYY-MM-DDTHH:MM:SSZ)`
- Пример:
```bash
--to 2011-01-01T00:00:00Z
```

`--format` (необязательный)
- Описание: Формат вывода результатов анализа.
- Поддерживаемые значения:
adoc — вывод в формате AsciiDoc.
markdown — вывод в формате Markdown.
- Формат значения: `<format>`
Пример:
```bash
--format markdown
```

`--filter-field` (необязательный)
- Описание: Имя поля для фильтрации логов.
- Поддерживаемые значения:
    - `address` — IP-адрес клиента. Пример: 192.168.1.1.
    - `method` — HTTP-метод запроса. Пример: GET, POST.
    - `url` — URL запрашиваемого ресурса. Пример: /index.html.
    - `protocol` — Версия протокола HTTP. Пример: HTTP/1.1, HTTP/2.0.
    - `response-code` — HTTP-код ответа сервера. Пример: 404, 200.
    - `response-size` — Размер ответа сервера в байтах. Пример: 1024.
    - `referer` — Реферер (поле Referer). Пример: https://google.com.
    - `user-agent` — User-Agent клиента. Пример: Mozilla/5.0.
- Формат значения: `<field>`
- Пример:
```bash
--filter-field method
```

`--filter-value` (необязательный)
- Описание: Значение фильтра для указанного поля (`--filter-field`).
- Формат значения: `<value>`
- Пример:
```bash
--filter-field method --filter-value GET
```

# Примеры использования
Пример 1:
- Ввод:

>```bash
>sbt run --path https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs,src/main/resources/*.txt --format markdown --from 2009-01-01T00:00:00Z --to 2011-01-01T00:00:00Z
>```
- Вывод:

#### Общая информация

| Метрика                 | Значение           |
|:------------------------|-------------------:|
| Файл(-ы)                | `https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs`, `logs_fix.txt`, `test.txt`   |
| Начальная дата          | 13.11.2009 |
| Конечная дата           | 13.11.2010   |
| Количество запросов     | 2       |
| Средний размер ответа   | 1694b  |
| 95p размера ответа      | 2423b      |


#### Коды ответа

| Код | Имя               | Количество         |
|:---:|:------------------|-------------------:|
| 200 | OK | 2 |

#### User-Agents

| User-Agent              | Количество         |
|:------------------------|-------------------:|
| `Mozilla` | 2 |

#### HTTP-методы

| Метод       | Количество         |
|:------------|-------------------:|
| `GET` | 2 |

#### Запрашиваемые ресурсы

| Ресурс                  | Количество         |
|:------------------------|-------------------:|
| `/Operative/Robust/test_resource.test` | 1 |
| `/Mandatory/test_resource.test` | 1 |

Пример 2:
- Ввод:

>```bash
>sbt run --path src/main/resources/logs_fix.txt --format markdown --filter-field url --filter-value logistical.css
>```
- Вывод:


#### Общая информация

| Метрика                 | Значение           |
|:------------------------|-------------------:|
| Файл(-ы)                | `logs_fix.txt`   |
| Начальная дата          | 13.11.2024 |
| Конечная дата           | 13.11.2024   |
| Количество запросов     | 23       |
| Средний размер ответа   | 1760b  |
| 95p размера ответа      | 2909b      |


#### Коды ответа

| Код | Имя               | Количество         |
|:---:|:------------------|-------------------:|
| 200 | OK | 19 |
| 301 | Moved Permanently | 2 |
| 500 | Internal Server Error | 2 |

#### User-Agents

| User-Agent              | Количество         |
|:------------------------|-------------------:|
| `Mozilla` | 21 |
| `Opera` | 2 |

#### HTTP-методы

| Метод       | Количество         |
|:------------|-------------------:|
| `GET` | 14 |
| `DELETE` | 4 |
| `POST` | 1 |
| `HEAD` | 4 |

#### Запрашиваемые ресурсы

| Ресурс                  | Количество         |
|:------------------------|-------------------:|
| `/logistical.css` | 5 |
| `/policy-Optimized/project/logistical.css` | 1 |
| `/Streamlined/global/analyzer-Inverse%20logistical.css` | 1 |
| `/Public-key/explicit/logistical.css` | 1 |
| `/Stand-alone-asynchronous/4th%20generation/Visionary/logistical.css` | 1 |
| `/Robust/logistical.css` | 1 |
| `/Front-line_definition/logistical.css` | 1 |
| `/Phased/Virtual/logistical.css` | 1 |
| `/User-centric_Sharable%20Cross-platform-contingency-logistical.css` | 1 |
| `/Persevering-implementation-logistical.css` | 1 |