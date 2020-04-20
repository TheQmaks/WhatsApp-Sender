package qmaks;

import java.io.*;
import java.util.List;
import java.util.Random;
import qmaks.utils.Point;
import java.nio.file.Files;
import java.util.Properties;
import java.util.regex.Pattern;
import java.nio.charset.Charset;
import se.vidstige.jadb.RemoteFile;
import static qmaks.utils.HTTP.*;
import static qmaks.utils.Android.*;

/**
 * @author Qmaks
 * @project WhatsApp Sender
 */
public class WhatsAppSender {
    public static void main(String[] args) {
        try {
            List<String> proxyList = Files.readAllLines(new File("proxy.txt").toPath(), Charset.defaultCharset());
            log("[*] Загрузили " + proxyList.size() + " прокси.");
            BufferedReader numbers = new BufferedReader(new FileReader("numbers.txt"));
            log("[*] Загрузили список номеров.");
            Properties config = new Properties();
            config.load(new InputStreamReader(new FileInputStream("config.txt"), Charset.forName("UTF-8")));
            log("[*] Загрузили кофигурацию.");

            startService("qmaks.ru2clip/.ClipboardService");
            log("[*] Запущен собственный сервис для передачи русских символов.");

            log("[*] Запускаем цикл прокси.");
            proxyLoop:
            for (String proxy : proxyList) {
                log("[*] Устанавливам прокси.");
                setProxy(proxy);
                log("[*] Регистрируем аккаунт.");
                registerAccount();

                log("[*] Запускаем цикл рассылки сообщений.");
                senderLoop:
                for (int num = 0; num < Integer.valueOf(config.getProperty("numbersCount")); num++) {
                    String number = numbers.readLine();
                    if (number != null) {
                        log("[*] Начинаем спам номера " + number);

                        log("[*] Генерируем сообщение.");
                        StringBuilder message = new StringBuilder();
                        String[] parts = config.getProperty("message").split(Pattern.quote("[split]"));
                        for(String part : parts) {
                            if(part.contains("[*]")) {
                                String[] variations = part.split(Pattern.quote("[*]"));
                                message.append(variations[new Random().nextInt(variations.length)]);
                            } else {
                                message.append(part);
                            }
                        }

                        spamNumber(number, message.toString());
                        Thread.sleep(Long.valueOf(config.getProperty("delay")));
                    } else {
                        log("[*] Закончились номера под рассылку.");

                        log("[*] Очищаем информацию приложения WhatsApp");
                        clearData("com.whatsapp");

                        break proxyLoop;
                    }
                }

                log("[*] Очищаем информацию приложения WhatsApp");
                clearData("com.whatsapp");
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
    }

    public static void setProxy(String proxy) throws Exception {
        log("[1] Парсим информацию о прокси.");
        String login = proxy.split("@")[0].split(":")[0];
        String password = proxy.split("@")[0].split(":")[1];
        String socksIp = proxy.split("@")[1].split(":")[0];
        String socksPort = proxy.split("@")[1].split(":")[1];

        log("    [*] Текущий прокси: " + proxy.split("@")[1]);

        log("[2] Запускаем ProxyDroid.");
        startActivity("org.proxydroid/.ProxyDroid");

        log("[3] Проверяем запущен ли на данный момент прокси.");
        if (getValueByXPath("//node[@resource-id='android:id/switchWidget']/@checked").equals("true")) {
            log("    [*] Отключаем прокси.");
            tap(new Point(650, 275));
        }

        log("[4] Начат процесс смены IP адреса.");
        //Открываем поле ввода IP
        tap(new Point(65, 755)); //Открыли окно для ввода IP
        log("    [4.1] Открыто окно ввода IP.");

        String ip = getValueByXPath("//node[@resource-id='android:id/edit']/@text");
        log("    [4.2] Сдампили текущий интерфейс и получили значение xpath.");
        for (int i = 0; i < ip.length(); i++) {
            pressKey(112); //Нажимаем DEL
        }
        log("    [4.3] Очистили содержимое окна.");

        writeText(socksIp);//Пишем IP прокси
        log("    [4.4] Вписали новый IP.");
        tap(new Point(650, 875)); //Нажимаем кнопку "ОК"
        log("    [4.5] Применили новый IP.");
        log("    [4.6] Смена IP адреса завершена.");

        log("[5] Начат процесс смены порта.");
        tap(new Point(65, 880)); //Открываем окно для ввода порта
        log("    [5.1] Открыто окно ввода порта.");

        String port = getValueByXPath("//node[@resource-id='android:id/edit']/@text");
        log("    [5.2] Сдампили текущий интерфейс и получили значение xpath.");
        for (int i = 0; i < port.length(); i++) {
            pressKey(112); //Нажимаем DEL
        }
        log("    [5.3] Очистили содержимое окна.");

        writeText(socksPort);//Пишем порт прокси
        log("    [5.4] Вписали новый порт.");
        tap(new Point(650, 875)); //Нажимаем кнопку "ОК"
        log("    [5.5] Применили новый порт.");
        log("    [5.6] Смена порта завершена.");

        log("[6] Пролистываем список элементов.");
        swipe(new Point(450, 1120), new Point(450, 320), 650);

        log("[7] Начат процесс смены логина.");
        tap(getPointByBounds(getValueByXPath("//node[@text='Пользователь']/@bounds"))); //Открываем окно для ввода логина
        log("    [7.1] Открыто окно для ввода логина.");

        String log = getValueByXPath("//node[@resource-id='android:id/edit']/@text");
        log("    [7.2] Сдампили текущий интерфейс и получили значение xpath.");
        for (int i = 0; i < log.length(); i++) {
            pressKey(112); //Нажимаем DEL
        }
        log("    [7.3] Очистили содержимое окна.");

        writeText(login);//Пишем логин прокси
        log("    [7.4] Вписали новый логин.");
        tap(new Point(650, 875)); //Нажимаем кнопку "ОК"
        log("    [7.5] Применили новый логин.");
        log("    [7.6] Смена логина завершена.");

        log("[8] Начат процесс смены пароля.");
        tap(getPointByBounds(getValueByXPath("//node[@text='Пароль']/@bounds"))); //Открываем окно для ввода логина
        log("    [8.1] Открыто окно для ввода пароля.");

        doubleTap(new Point(99, 748));
        log("    [8.2] Выделили весь текст.");
        pressKey(112); //Нажимаем DEL
        log("    [8.3] Очистили содержимое окна.");

        writeText(password);//Пишем пароль прокси
        log("    [8.4] Вписали новый пароль.");
        tap(new Point(650, 875)); //Нажимаем кнопку "ОК"
        log("    [8.5] Применили новый пароль.");
        log("    [8.6] Смена пароля завершена.");

        log("[9] Пролистываем список элементов.");
        swipe(new Point(450, 320), new Point(450, 1120), 650);

        tap(new Point(778, 275));
        log("[10] Включили прокси.");
    }

    public static void registerAccount() throws Exception {
        log("[1] Открываем окно принятия правил перед регистрацией.");
        startActivity("com.whatsapp/.registration.EULA");

        log("[2] Ожидаем загрузки окна.");
        waitLoading("//node[@resource-id='com.whatsapp:id/eula_accept']/@text", "Принять и продолжить");

        log("[3] Нажимаем на кнопку 'Принять и продолжить'.");
        tap(new Point(450, 1370));

        log("[4] Запускаем главный цикл регистрации.");
        mainLoop:
        while (true) {
            log("[5] Получаем текущий код страны.");
            String cc = getValueByXPath("//node[@resource-id='com.whatsapp:id/registration_cc']/@text");
            while (true) {
                if (cc.isEmpty()) {
                    cc = getValueByXPath("//node[@resource-id='com.whatsapp:id/registration_cc']/@text");
                } else {
                    break;
                }
            }

            log("[6] Переключаемся на поле для ввода кода страны.");
            tap(new Point(206, 435));

            log("[7] Удаляем текущий код страны.");
            for (int i = 0; i < cc.length(); i++) {
                pressKey(112); //Нажимаем DEL
            }

            log("[8] Отправляем запрос на получение номера.");
            String response = getNumber();
            if (response.startsWith("ACCESS_NUMBER")) {
                log("    [*] Успешно получили номер и его id.");
            } else {
                switch (response) {
                    case "NO_MEANS":
                        log("    [*] Недостаточно средств на счету.");
                        break;
                    case "NO_NUMBER":
                        log("    [*] Нет номеров с заданными параметрами.");
                        break;
                    case "NO_ACTIVATORS":
                        log("    [*] В сети нет ни одного активатора.");
                        break;
                    case "NO_ACTIVATORS_OVERLOAD":
                        log("    [*] Все активаторы перегружены.");
                        break;
                    case "NO_ACTIVATORS_RATE":
                        log("    [*] Ставка активаторов выше вашей.");
                        break;
                    case "ERROR_SQL":
                        log("    [*] Ошибка SQL-сервера.");
                        break;
                    default:
                        break;
                }
                log("   [*] Запускаем цикл регистрации заново.");
                continue mainLoop;
            }

            log("[9] Записываем полученный номер.");
            writeText(response.split(":")[2]);

            log("[10] Нажимаем кнопку 'Далее'.");
            tap(new Point(450, 1350));

            log("[11] Ждем сообщения с подтверждением.");
            waitPrint("Вы согласны или хотите изменить номер?");

            log("[12] Подтверждаем номер.");
            tap(new Point(750, 880));

            log("[13] Парсим id с сайта.");
            int id = Integer.valueOf(response.split(":")[1]);

            log("[14] Устанавливаем статус готовности.");
            setStatus(id, 1);

            log("[15] Запускаем цикл получения кода.");
            codeLoop:
            while (true) {
                log("   [*] Проверяем появился ли код.");
                response = getStatus(id);
                if (response.startsWith("STATUS_OK")) {
                    log("[16] Записываем полученный код.");
                    writeText(response.split(":")[1]);

                    log("[17] Запускаем цикл проверки текущего окна.");
                    while (true) {
                        log("[18] Получаем текущее значение по xpath.");
                        String value = getValueByXPath("//node[@class='android.widget.TextView'][1]/@text");
                        if (value.contains("Введен неверный код")) {
                            log("   [*] Устанавливаем статус неверного кода.");
                            setStatus(id, 3);
                            log("   [*] Нажимаем кнопку 'ОК'.");
                            tap(new Point(750, 800));
                            log("   [*] Запускаем новый процесс получения кода.");
                            continue codeLoop;
                        } else if (value.contains("Профиль")) {
                            log("   [*] Перешли на вкладку 'Профиль', завершаем цикл.");
                            break codeLoop;
                        }
                    }
                } else {
                    switch (response) {
                        case "STATUS_CANCEL":
                            log("   [*] Активатор отменил активацию.");
                            log("   [*] Нажимаем по тексту 'Неверный номер'.");
                            tap(new Point(550, 240));
                            continue mainLoop;
                        case "STATUS_WAIT_RESEND":
                            log("   [*] Активатор запросил переотправку смс.");
                            log("   [*] Ждем досупности кнопки 'Отправить снова'.");
                            waitLoading("//node[@resource-id='com.whatsapp:id/resend_sms_btn']/@enabled", "true");
                            log("   [*] Кликаем по кнопке 'Отправить снова'.");
                            tap(new Point(150, 480));
                            log("   [*] Подтверждаем статус отправленного кода.");
                            setStatus(id, 1);
                            break;
                        default:
                            break;
                    }
                    log("   [*] Запускаем цикл получения кода заново.");
                    continue codeLoop;
                }
            }

            log("[15] Устанавливаем статус успешной активации.");
            setStatus(id, 6);
            log("[16] Завершаем цикл.");
            break mainLoop;
        }

        log("[17] Вводим имя.");
        writeText("Roman");

        log("[18] Нажимаем кнопку 'Далее'.");
        tap(new Point(450, 1350));
    }

    public static void spamNumber(String number, String message) throws Exception {
        log("[1] Добавляем номер в список контактов.");
        addNumber(number);

        log("[2] Запускаем цикл проверки текущего интерфейса.");
        while (true) {
            log("[3] Получаем значение по xpath.");
            String value = getValueByXPath("//node[@class='android.widget.Button'][1]/@text");
            log("[4] Проверяем значения.");
            if (value.equals("Добавить поле")) {
                log("   [*] Нажимаем на кнопку 'Готово'.");
                tap(new Point(100, 100));
                break;
            } else if (value.equals("Сохранить локально")) {
                log("   [*] Нажимаем на кнопку 'Сохранить локально'.");
                tap(new Point(250, 850));
                log("   [*] Нажимаем на кнопку 'Готово'.");
                tap(new Point(100, 100));
                break;
            }
        }

        log("[5] Ждем загрузки информации о контакте.");
        long time = System.currentTimeMillis();
        while(true) {
            if((System.currentTimeMillis() - time) >= 60000L) {
                log("   [*] Вероятнее всего данный пользователь не зарегистрирован в WhatsApp!");

                log("   [5.1] Открываем параметры.");
                tap(new Point(850, 100));

                log("   [5.2] Ждем загрузки.");
                waitLoading("//node[@text='Удалить']/@text", "Удалить");

                log("   [5.3] Удаляем контакт.");
                tap(new Point(400, 400));

                log("   [5.4] Ждем загрузки.");
                waitLoading("//node[@resource-id='android:id/button1']/@text", "ОК");

                log("   [5.5] Подтверждаем.");
                tap(getPointByBounds(getValueByXPath("//node[@resource-id='android:id/button1']/@bounds")));

                log("   [5.6] Переходим к следующему номеру.");
                return;
            } else {
                if (getValueByXPath("//node[@resource-id='com.android.contacts:id/network_title']/@text").equals("WhatsApp")) {
                    break;
                }
            }
        }

        log("[6] Запускаем слушатель событий.");
        InputStream events = noxEmulator.executeShell("uiautomator events", new String[0]);
        BufferedReader eventsListener = new BufferedReader(new InputStreamReader(events));

        log("[7] Открываем окно создания группы.");
        startActivity("com.whatsapp/.GroupMembersSelector");

        log("[8] Ждем загрузки меню.");
        for (String line = eventsListener.readLine(); line != null; line = eventsListener.readLine()) {
            if(line.contains("Новая группа")) {
                events.close();
                eventsListener.close();
                break;
            }
        }

        log("[9] Кликаем по контакту.");
        tap(new Point(250, 250));

        log("[10] Нажимаем далее.");
        tap(new Point(810, 1350));

        log("[11] Ждем загрузки.");
        waitLoading("//node[@resource-id='com.whatsapp:id/group_name']/@text", "Введите тему…");

        log("[12] Записываем название группы.");
        writeText("Group");

        log("[13] Подтверждаем создание группы.");
        tap(new Point(810, 400));

        log("[14] Ждем загрузки.");
        ////node[@text='Вы создали группу "Group"']/@text
        while(true) {
            if(getValueByXPath("//node[@resource-id='com.whatsapp:id/conversation_contact_name']/@text").equals("Group")) {
                break;
            } else if(getValueByXPath("node[@text='Вы создали группу \"Group\"']/@text").equals("Вы создали группу \"Group\"")) {
                log("   [*] Открываем группу.");
                tap(getPointByBounds(getValueByXPath("//node[@resource-id='com.whatsapp:id/conversation_contact_name']/@bounds")));
                break;
            }
        }

        log("[15] Загружаем рандомную картинку.");
        File[] pics = new File("pics").listFiles();
        File pic = pics[new Random().nextInt(pics.length)];
        noxEmulator.push(pic, new RemoteFile("/storage/emulated/legacy/Download/" + pic.getName()));

        log("[16] Обновляем список медиа-файлов.");
        refreshMedia();

        log("[17] Получаем координаты кнопки 'Прикрепить' и кликаем по ней.");
        tap(getPointByBounds(getValueByXPath("//node[@resource-id='com.whatsapp:id/input_attach_button']/@bounds")));

        log("[18] Ждем загрузки меню.");
        Thread.sleep(1000L);

        log("[19] Нажимаем на кнопку 'Галерея'.");
        tap(new Point(640, 995));

        log("[20] Ждем загрузки.");
        waitLoading("//node[@text='Все медиа']/@text", "Все медиа");

        log("[21] Открываем 'Все медиа'.");
        tap(getPointByBounds(getValueByXPath("//node[@text='Все медиа']/@bounds")));

        log("[22] Ждем загрузки.");
        waitLoading("//node[@text='Все медиа']/@class", "android.widget.TextView");

        log("[23] Нажимаем на картинку.");
        tap(getPointByBounds(getValueByXPath("//node[@class='android.widget.ImageView']/@bounds")));

        log("[24] Ждем загрузки.");
        waitLoading("//node[@resource-id='com.whatsapp:id/send']/@content-desc", "Отправить");

        log("[25] Нажимаем 'Добавить подпись'.");
        tap(new Point(100, 1370));

        log("[26] Копируем сообщение для спама в буфер обмена.");
        toClipboard(message);

        log("[27] Вызываем долгое нажатие.");
        doubleTap(new Point(100, 1370));

        log("[28] Нажимаем 'Вставить'.");
        tap(new Point(100, 1300));

        log("[29] Отправляем.");
        tap(getPointByBounds(getValueByXPath("//node[@resource-id='com.whatsapp:id/send']/@bounds")));

        log("[30] Ждем загрузки.");
        waitLoading("//node[@resource-id='com.whatsapp:id/entry']/@text", "Введите текст");

        log("[31] Открываем инфо о группе.");
        tap(new Point(190, 110));

        log("[32] Ждем загрузки.");
        waitLoading("//node[@resource-id='com.whatsapp:id/conversation_contact_name_scaler']/node/@text", "Group");

        log("[33] Пролистываем список элементов.");
        swipe(new Point(450, 1120), new Point(450, 320), 650);

//        log("[35] Получаем координаты элемента номера и кликаем по нему.");
//        tap(getPointByBounds(getValueByXPath("//node[@text='" + number + "']/@bounds")));
        log("[34] Кликаем по номеру.");
        tap(new Point(150, 970));

        log("[35] Ждем загрузки.");
        waitLoading("//node[@text='Написать " + number +"']/@text", "Написать " + number);

        log("[36] Удаляем контакт из группы.");
        tap(new Point(400, 850));

        log("[37] Ждем загрузки.");
        waitLoading("//node[@resource-id='android:id/message']/@text", "Удалить " + number + " из группы \"Group\"?");

        log("[38] Подтверждаем удаление контакта из группы.");
        tap(new Point(760, 790));

        log("[39] Выходим из группы.");
        tap(getPointByBounds(getValueByXPath("//node[@resource-id='com.whatsapp:id/exit_group_text']/@bounds")));

        log("[40] Ждем загрузки.");
        waitLoading("//node[@resource-id='android:id/message']/@text", "Выйти из группы \"Group\"?");

        log("[41] Подтверждаем выход из группы.");
        tap(new Point(750, 800));

        log("[42] Ждем загрузки.");
        waitLoading("//node[@resource-id='com.whatsapp:id/exit_group_text']/@text", "Удалить группу");

        log("[43] Удаляем группу.");
        tap(getPointByBounds(getValueByXPath("//node[@text='Удалить группу']/@bounds")));

        log("[44] Ждем загрузки.");
        waitLoading("//node[@resource-id='android:id/message']/@text", "Удалить группу \"Group\"?");

        log("[45] Подтверждаем удаление группы.");
        tap(new Point(730, 840));

        log("[46] Открываем записную книгу.");
        startActivity("com.android.contacts/.activities.PeopleActivity");

        log("[47] Ждем загрузки.");
        waitLoading("//node[@resource-id='com.android.contacts:id/cliv_name_textview']/@text", number);

        log("[48] Открываем контакт.");
        tap(new Point(180, 480));

        log("[49] Ждем загрузки.");
        waitLoading("//node[@resource-id='android:id/action_bar_title']/@text", number);

        log("[50] Открываем параметры.");
        tap(new Point(850, 100));

        log("[51] Ждем загрузки.");
        waitLoading("//node[@text='Удалить']/@text", "Удалить");

        log("[52] Удаляем контакт.");
        tap(new Point(400, 400));

        log("[53] Ждем загрузки.");
        waitLoading("//node[@resource-id='android:id/button1']/@text", "ОК");

        log("[54] Подтверждаем.");
        tap(getPointByBounds(getValueByXPath("//node[@resource-id='android:id/button1']/@bounds")));

        log("[55] Удаляем картинку.");
        removeFile("/storage/emulated/legacy/Download/" + pic.getName());

        log("[56] Обновляем список медиа-файлов.");
        refreshMedia();
    }

    private static void log(String s) {
        System.out.println(s);
    }
}