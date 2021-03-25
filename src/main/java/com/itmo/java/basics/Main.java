package com.itmo.java.basics;

import com.itmo.java.basics.exceptions.DatabaseException;
import com.itmo.java.basics.logic.Database;
import com.itmo.java.basics.logic.Segment;
import com.itmo.java.basics.logic.impl.DatabaseImpl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Path dbPath = Paths.get("C:/Users/NIKITOS");

        try {
            Database database = DatabaseImpl.create("FindMePlease", dbPath);
            database.createTableIfNotExists("table1");
            database.createTableIfNotExists("table2");
            database.write("table1", "1", null);
            System.out.println(database.read("table1", "1"));
            System.out.println(database.read("table1", "4563"));
            database.write("table1", "2", "1".getBytes(StandardCharsets.UTF_8));
            database.write("table1", "3", "1".getBytes(StandardCharsets.UTF_8));
            database.write("table2", "Hel__123lo", "25ю.фыафы1241лдаара132134512".getBytes(StandardCharsets.UTF_8));
            database.write("table2", "Hel__123lo", "2132134512".getBytes(StandardCharsets.UTF_8));
            database.write("table2", "Hel1lo", "2a1s2ффодпрфрююфа.ю.ю._d3".getBytes(StandardCharsets.UTF_8));
            database.write("table2", veryLongText, veryLongText.getBytes(StandardCharsets.UTF_8));
            database.write("table2", "veryLongText", veryLongText.getBytes(StandardCharsets.UTF_8));
            database.write("table1", veryLongText, veryLongText.getBytes(StandardCharsets.UTF_8));
            database.write("table1", "veryLongText", veryLongText.getBytes(StandardCharsets.UTF_8));


            var a1 = database.read("table1", "2");
            var b1 = a1.get();
            var c1 = new String(b1, StandardCharsets.UTF_8);
            System.out.println(c1);

            a1 = database.read("table1", "2");
            b1 = a1.get();
            c1 = new String(b1, StandardCharsets.UTF_8);
            System.out.println(c1);

            database.delete("table1", "1");

            a1 = database.read("table1", "1");
            if (a1.isPresent()) {
                b1 = a1.get();
                c1 = new String(b1, StandardCharsets.UTF_8);
                System.out.println(c1);
            } else {
                System.out.println("Bad");
            }

            database.write("table1", "1", "132134512".getBytes(StandardCharsets.UTF_8));

            a1 = database.read("table1", "1");
            if (a1.isPresent()) {
                b1 = a1.get();
                c1 = new String(b1, StandardCharsets.UTF_8);
                System.out.println(c1);
            }

            a1 = database.read("table1", "3");
            if (a1.isPresent()) {
                b1 = a1.get();
                c1 = new String(b1, StandardCharsets.UTF_8);
                System.out.println(c1);
            }


            a1 = database.read("table2", "1");
            if (a1.isPresent()) {
                b1 = a1.get();
                c1 = new String(b1, StandardCharsets.UTF_8);
                System.out.println(c1);
            }
            var a = database.read("table1", "2");
            if (a.isPresent()) {
                var b = a.get();
                var c = new String(b, StandardCharsets.UTF_8);
                System.out.println(c);
            }
            a = database.read("table1", veryLongText);
            if (a.isPresent()) {
                var b = a.get();
                var c = new String(b, StandardCharsets.UTF_8);
                System.out.println(c);
            }
            database.delete("table1", veryLongText);
            a = database.read("table1", veryLongText);
            if (a.isPresent()) {
                var b = a.get();
                var c = new String(b, StandardCharsets.UTF_8);
                System.out.println(c);
            }
        } catch (DatabaseException e) {
            System.out.println(e.getMessage());
        }
    }

    static String veryLongText = "СТО ТЫСЯЧ СЛОВ Светлана БИТКИНА\n" +
            "\n" +
            "И вот книга закончена. У нее даже есть название — «Сто тысяч слов». Помог компьютер, считающий знаки, слова и строки. Оказалось, что в моем тексте всего 100 тысяч слов. Подумалось, как много можно рассказать с помощью такого, в общем-то небольшого, количества. Интересно, а сколько слов каждый из нас произносит в течение своей жизни? Наверное, многие и многие миллионы…\n" +
            "\n" +
            "ГЛОБАЛЬНОЕ И ЛОКАЛЬНОЕ\n" +
            "\n" +
            "Как могла возникнуть идея стать журналистом у ребенка, знакомого лишь с «Пионерской правдой» и «Мурзилкой»? А произошло это случайно. Посмотрела фильм Сергея Герасимова «Журналист». И образ сотрудника центральной газеты, который по письму в редакцию отправляется на поиски истины в далекий сибирский город, засел в голове.\n" +
            "\n" +
            "Но, наверное, так бы он там и остался, если бы не любимый преподаватель истории Лидия Ивановна Чанкина. Как-то на перемене она спросила нас, девчонок, задержавшихся в классе, кем мы хотим стать. Одна из моих одноклассниц ответила: «Партийным работником», а я неожиданно для самой себя выпалила: «Журналистом»!\n" +
            "\n" +
            "Лидия Ивановна посмотрела на меня внимательно и сказала: «Чтобы поступать на журналистику, надо иметь публикации в газете». (Как я поняла, кто-то из ее учеников уже прошел этот путь). И, увидев в моих глазах вопрос, посоветовала обратиться в областную молодежную газету «Комсомольская искра».\n" +
            "\n" +
            "Не помню, был ли это совет учительницы или моя инициатива, но в редакцию «КИ», которая находилась в двухэтажном доме в начале Октябрьского проспекта, я отнесла заметку о политинформации в классе. Редакция занимала правое крыло первого этажа здания обкома ВЛКСМ. Меня встретил заведующий отделом учащейся молодежи (конечно, это я узнала потом) Александр Борисович Зарецкий. Прочитав информацию, он произнес: «Это слишком локальное событие, надо написать о чем-то более глобальном». На беду, я не знала ни что такое локальное, ни что такое глобальное, но уходила с твердым намерением вернуться.\n" +
            "\n" +
            "Следующая заметка, которую написала для областной молодежки, была посвящена пионерскому сбору в подшефном классе. Зарецкий ее принял. И все последующие дни я с волнением ждала публикации в газете. Тут признаюсь, что это чувство не покинуло меня и сейчас. Отправив очередную работу в редакцию, каждый раз с нетерпением жду ее появления на газетной полосе.\n" +
            "\n" +
            "А та публикация все-таки состоялась. Но я услышала о ней не первая. Информация о моем дебюте пришла из учительской. Газету с текстом своей ученицы передавали из рук в руки, и владимирская школа №19 буквально бурлила от радости. Наконец, дошла очередь и до меня. Но когда я прочитала свой первый увидевший свет опус, то не узнала в нем ни одного слова!\n" +
            "\n" +
            "Как ни странно, это ничуть не задело авторского самолюбия. Главное –внизу стояла моя фамилия, меня опубликовали! Для обязательного творческого конкурса при поступлении на журфак требовалось не менее трех публикаций в газете. И я была близка к цели!\n" +
            "\n" +
            "Тогда все журналисты казались мне людьми особенными, сродни небожителям. У промтоварного магазина возле троллейбусной остановки «Политехнический институт», как раз по дороге в школу, стояли три телефонные будки. И чтобы войти в одну из них и набрать номер Александра Борисовича, приходилось собирать в кулак всю свою смелость. Ее хватало ровно на то, чтобы, услышав на другом конце провода Зарецкого, дрожащим голосом спросить: «Я написала. Можно принести?»\n" +
            "\n" +
            "Считала, что идти в редакцию с пустыми руками неприлично. И, вернувшись домой, садилась за очередную заметку. Несмотря на то, что количество печатных работ вроде бы уже достигло необходимого предела, этого занятия не бросала. В газету тянул не только «творческий конкурс», но и творческая атмосфера, царившая в редакционном коллективе, где каждый был интересен и по-своему уникален. И когда я организовала в школе Клуб интересных встреч, то одними из первых пригласила туда Сашу Зарецкого и Эллу Рогожанскую.\n" +
            "\n" +
            "НА ЗАВОДЕ\n" +
            "\n" +
            "Общение с редакцией пришлось прервать летом. В годы моей школьной юности после окончания 9-го класса полагалась обязательная производственная практика. Весь наш 9 «В» оказался на подшефном тракторном заводе. Большинство — в тарном цехе. Мальчишки ни шатко ни валко сколачивали до обеда ящики. А те, кому повезло с родителями, трудились полную смену в цехах наравне со взрослыми.\n" +
            "\n" +
            "Мне повезло. Мама устроила меня фрезеровщицей в цех шестерен, где сама работала мастером после окончания Харьковского машиностроительного техникума. В моем распоряжении было шесть фрезерных станков — только успевай вставлять стальные заготовки. В первую смену еще терпимо: с утра до обеда прохладно, цех не успевал прогреться от солнца и работающих станков. А вот во вторую — сущий ад. С 16 до 24 часов от раскаленной стеклянной крыши и оборудования здесь было пекло.\n" +
            "\n" +
            "Как ни старайся, а все равно к концу смены по уши в масле, от которого до конца не отмыться даже в душе, так оно въедается в поры. Помылся, оделся — и домой приходишь только к часу ночи. Пока проснешься утром, пока поешь, едва придешь в себя, а нужно уже собираться на работу.\n" +
            "\n" +
            "На заводе выпускалась своя многотиражка — «Тракторостроитель». Эта газета была одной из самых крупных многотиражек в области. Ее тогдашний штат можно сравнить с нынешним штатом районной газеты. В ней трудились: редактор, его заместитель, три корреспондента, технический сотрудник и машинистка.\n" +
            "\n" +
            "По совету мамы я зашла в заводскую редакцию. Встретила меня редактор, пожилая, очень доброжелательная женщина, внимательно выслушала, поддержала намерение сотрудничать во время практики. И я решила, что в свободное от работы время буду обязательно писать. Но так и не написала ни одной заметки.\n" +
            "\n" +
            "Посвятить себя журналистике можно было только перед второй сменой. Но какое там! Работа за станками так изматывала, что времени едва хватало на то, чтобы поспать и восстановить силы до начала очередного трудового дня. Так что продолжить заниматься журналистикой я смогла только с нового учебного года, в 10-м классе.\n" +
            "\n" +
            "Ради этого пришлось забросить баскетбольную секцию, в которой тренировалась с четвертого класса, и сборную области. На занятия спортом времени уже не оставалось. Ведь несколько раз в неделю ездила в редакцию, где работали блестящие журналисты и прекрасные люди: Александр Зарецкий, Виталий Волков, его однофамилица Алла Волкова, фотограф Виктор Улитин, Юля Артемьева, Валя Левушкина (Улитина), редактор Александр Иванович Кузнецов, водитель-фронтовик Петр Маркович. И туда тянуло все сильнее и сильнее.\n" +
            "\n" +
            "ЭЛЛА И СУХОМЛИНСКИЙ\n" +
            "\n" +
            "В школе начался новый учебный год, а в «Комсомольской искре» у меня появился новый шеф. Место Александра Зарецкого, которого из отдела учащейся молодежи перевели заведовать отделом комсомольской жизни, заняла молоденькая выпускница МГУ Элла Михайловна Рогожанская. После окончания журфака она поработала в александровской районке «Голос труда». А теперь пошла на повышение в областную молодежную газету. Миниатюрная, с точеной фигуркой, серыми очень внимательными глазами.\n" +
            "\n" +
            "Несмотря на смену шефа, я продолжала заходить в редакцию «за заданием» хотя бы раз в три дня. Думаю, долговязая старшеклассница, доставшаяся в наследство от Саши Зарецкого, вряд ли вызывала восторг у моей новой наставницы. Позже Элла призналась, что, завидев своего юного автора в окне, пряталась в шкаф. Конечно, я об этом не догадывалась. Но хорошо помню, как росло мое мастерство под ее чутким, но требовательным и строгим руководством. Заметки, публиковавшиеся в газете, увеличивались в объеме, и в них я уже узнавала себя.\n" +
            "\n" +
            "Как-то зимой Элла даже взяла меня с собой в командировку в Лакинск, где работала школьная обсерватория. И дала мастер-класс по сбору фактуры для зарисовки и очерка. Особенно строго она спрашивала за грамотность. Советовала писать дома диктанты, что я и делала с помощью брата. И даже пришла ко мне в школу на выпускной экзамен по литературе. Не знаю, как ей удалось договориться об этом с учителями, но я не сдала сочинение, пока она не пробежала его глазами и утвердительно не кивнула головой.\n" +
            "\n" +
            "Сочетать учебу, увлечение журналистикой да еще общественную работу было непросто, а ближе к окончанию школы становилось все сложнее. Но и тут Элла подсказала выход. Дала почитать книгу известного педагога Василия Сухомлинского. В ней он развивал интересную теорию подготовки к урокам, которая мне явно подходила.\n" +
            "\n" +
            "Суть этого метода в том, что ученик, придя из школы, занимается любимым делом, а уроки учит утром. То, что после школы занимало уйму времени, рано утром на свежую голову требовало не больше двух часов.\n" +
            "\n" +
            "Вечером учишь предметы, которых не будет на вступительных экзаменах: математику, геометрию, физику, химию. Ложишься спать пораньше, в 9 часов вечера, чтобы встать до начала школьных занятий, в 5 утра, и готовить любимые, гуманитарные.\n" +
            "\n" +
            "Метод понравился, хотя, чтобы встать рано утром, требовалось сделать над собой усилие. Но это того стоило. На свежую голову и училось и думалось быстрее. С тех пор я поняла, что раннее время — самое подходящее для творчества. Наука Сухомлинского пригодится и позднее, когда стану работать в газете. Именно ранним утром всегда пишется легче, находятся нужные слова, разрешаются сложные вопросы, уменьшается сопротивление материала. И многое, над чем билась накануне, не зная, как выразить, становится особенно ясным, словно пелена с глаз спадает. Помогла такая рационализация времени и когда появились дети.\n" +
            "\n" +
            "Метод Сухомлинского оказался таким полезным, что я даже решила пропагандировать его среди одноклассников. Они уточнили мой распорядок дня. А потом в 9 часов вечера пришли к нашему частному дому на улице Народной постучали в окно моей комнаты. Решили проверить, не обманывает ли их староста, действительно ли уже видит сны. К сожалению, этот вечер оказался исключением. Я писала заметку для «Комсомольской искры» и задержалась за письменным столом. Позже мне не один раз припомнили это. Но я не обижалась.\n" +
            "\n" +
            "ОДНОКЛАССНИКИ\n" +
            "\n" +
            "Элла сагитировала меня поучаствовать в конкурсе сочинений «Золотое перо», который ежегодно проводил журнал «Журналист». Его победители получали право внеконкурсного поступления на журфаки страны.\n" +
            "\n" +
            "У самой Эллы уже был положительный опыт — ее талантливая ученица из Александрова, юнкор Нина Фокина, стала лауреатом этого конкурса и теперь училась в МГУ. Я видела ее один раз, когда она приезжала во Владимир к Элле, и смотрела на нее с нескрываемым восхищением. Второй раз встретилась с Ниной Семеновной, когда она уже работала заместителем ответственного секретаря «Российской газеты». А я, собственный корреспондент этой газеты во Владимирской области, приехала в командировку в московскую редакцию.\n" +
            "\n" +
            "Так вот, для конкурса требовалось сочинение на свободную тему. Я решила написать о своих одноклассниках. Все мы жили на соседних улицах — Народной, Литейной, Тракторной, были дружны. И у каждого имелось свое, отличавшее от других увлечение, которым он охотно делился. Об этом я и рассказала: «Хочу быть немного похожей на Фаю, немного на Лиду, немного на Надю, на Володю…» Это единственная строчка, которая запомнилась. Сочинение, несмотря на мое старание и Эллины надежды, в победители не вывело. Я получила вежливый и обтекаемый ответ из конкурсной комиссии. Жаль, что не сохранила его. Ведь собственно с него и начался отсчет моей журналистской биографии.\n" +
            "\n" +
            "А то, почти детское «Золотое перо» задало вектор на десятилетия вперед. По-прежнему люблю своих героев и стараюсь учиться у них.\n" +
            "\n" +
            "ЮЛИН ЦВЕТОК\n" +
            "\n" +
            "Заведующая отделом культуры «Комсомольской искры» Юлия Ивановна Артемьева казалась мне умудренной опытом журналисткой, а ведь Юле было всего 32 года! И сейчас, по прошествии более четырех десятков лет, думаю о ней с благодарностью за постоянную поддержку и интерес к моей судьбе.\n" +
            "\n" +
            "После неудачной попытки поступления на журфак МГУ я решила поступать в ЛГУ, потому что ленинградский журфак закончила Юля Артемьева. Своими яркими рассказами она смогла заронить любовь к городу, который я никогда не видела.\n" +
            "\n" +
            "Юля была необыкновенной: янтарные миндалевидные глаза, прямые каштановые волосы со стрижкой а ля Мирей Матье, красивый тонкий нос с горбинкой, завораживающая улыбка. Все находили ее похожей на Марину Цветаеву в молодости. Юлю отличало очень трепетное и строгое отношение к слову. Она считалась в редакции знатоком и ценителем поэзии. Поэты со всех концов не только Владимира, но и области ехали к Юле, чтобы услышать ее вердикт. Чуть ли не ежедневно почта приносила ей тугие конверты со стихами и рассказами. Думаю, эта эпидемия стихотворчества распространялась по земле владимирской не сама по себе, не случайно. Молодежь, да и люди солидного возраста, пытающиеся выразить свое «я» на бумаге, нашли в Юле того единственного и неповторимого критика и ценителя, в котором нуждались.\n" +
            "\n" +
            "Помню поэта Владимира Коваленкова, ученого-химика из Александрова, работавшего научным сотрудником в институте искусственных кристаллов. Он регулярно приезжал в «Комсомольскую искру». Видимо, поэтам было недостаточно Юлиной рецензии, присланной по почте. Хотелось и живого общения с таким человеком.\n" +
            "\n" +
            "Будучи неискушенным подростком, я упускала из виду симпатию, которой проникались к этой прекрасной женщине ее авторы. А теперь думаю, что поэты тянулись к Юле еще и потому, что были влюблены. Да разве можно было в нее не влюбиться? И ко всем, независимо от степени таланта, она относилась бережно, как к детям.\n" +
            "\n" +
            "С утра до вечера у нее в кабинете кипела на окне кофеварка. Такой кофе, как здесь, я пробовала потом только на студенческой практике в Армении. Часто мы пили его без сахара, с пластовым кисло-сладким сливовым мармеладом, который продавали в продуктовом магазине напротив стадиона «Торпедо».\n" +
            "\n" +
            "Был у Юли один большой, на мой взгляд, недостаток — она курила. Причем не баловалась, как некоторые, а по-настоящему, сигарета за сигаретой. Возможно, табак и послужил причиной тяжелой болезни почек. Понимала ли она всю ее серьезность? Не знаю. Но медицине официальной Юля не доверяла и по совету своей знакомой обратилась к московскому гомеопату. Периодически ездила на приемы и после привозила какие-то порошки и крошечные шарики. В то время, когда коллеги шли на обед в обкомовскую столовую, она оставалась в кабинете и принимала эти снадобья. И некому было отговорить ее от экспериментов над собой.\n" +
            "\n" +
            "В последний раз я видела Юлю в здании с колоннами на улице Фрунзе, в котором сейчас находится судмедэкспертиза, а тогда был диспансер. Мы с мамой пришли навестить больную. Юля говорила с нами, но нас уже не видела.\n" +
            "\n" +
            "А вскоре мне сказали, что она умерла. Это было в августе, незадолго до моего возвращения на учебу в Ленинград. На церемонию прощания я опоздала. Но не жалею об этом. Хорошо, что запомнила ее живой. Слышала, что урну с прахом дочери мать увезла на родину, в Бийск.\n" +
            "\n" +
            "В первый после окончания университета отпуск я загорелась мечтой совершить конное путешествие. Вдохновлял пример героини моего газетного очерка, тренера конноспортивной школы из поселка «Коммунар» Лены Муратовой. Она подсказала, что такие туристические маршруты есть на Алтае.\n" +
            "\n" +
            "Узнав, что планы на конный переход в горах рушатся, путевок нет, я не расстроилась. Остался пеший поход с рюкзаками по южному Алтаю, где точка отправления и возвращения — Бийск. В голове держала план навестить Юлину маму и могилу Юли. И это только укрепило решение взять железнодорожный билет до Барнаула.\n" +
            "\n" +
            "План удался. После трудного восхождения на горы южного Алтая и не менее трудного спуска, впечатляющей поездки на теплоходе по Телецкому озеру и реке Бие и возвращения в Бийск мы с моей спутницей Раей Левиной отправились в гости к Юлиной маме. В комнате на окне увидела кактус с цветами-звездами леопардовой расцветки, свисающими, как сережки, с зеленых «колбасок» с колючками. Точно такой же я видела дома у Юли.\n" +
            "\n" +
            "Как оказалось, это он и был — Юлина мама привезла его из Владимира. К сожалению, навестить могилу Юли на бийском кладбище, которую она разделила с отцом, не удалось. Мама сказала, что памятник осквернили вандалы, и ей неловко нас туда везти. Вот приведет все в порядок, и в следующий раз можно будет поехать.\n" +
            "\n" +
            "К сожалению, следующего раза не случилось.\n" +
            "\n" +
            "С собой, кроме меда и кедровых орехов, я привезла из далекого алтайского города отросток от Юлиного кактуса. Он много лет цвел в моей комнате. И, любуясь его пятнистыми цветами-звездами, я часто вспоминала свою любимую наставницу.\n" +
            "\n" +
            "МГУ\n" +
            "\n" +
            "Вместе с Харисом Ягудиным, выпускником школы комкоров, работавшей тогда при «Комсомольской искре», мы решили штурмовать МГУ. Здание столичного журфака я представляла похожим на владимирский Дом офицеров, бывший Дом Дворянского собрания: строгий, с колоннами, отделенный от дороги шеренгой лип. И воображение почти не обмануло.\n" +
            "\n" +
            "Не скрою — завидую нынешним выпускникам, проходящим тестирование. Нам пришлось сдавать экзамены — целых четыре! Причем все вузы страны принимали их с 1 августа. В случае провала других вариантов поступления не было. Только через год.\n" +
            "\n" +
            "А чтобы победить в конкурсе, следовало набрать как минимум 19 баллов — получить три «пятерки» и одну «четверку». Критикующие сегодня ЕГЭ, наверное, забыли, что каждый билет, который предлагалось вытащить абитуриенту на экзамене, состоял из трех вопросов. И ответить на них надо было без сучка без задоринки. А для этого проштудировать сотни, а то и тысячи параграфов по литературе, истории, иностранному языку. А представьте, что в случае неудачи, если все же решили не отступать от своей мечты, то же самое пришлось бы учить во второй, а то и в третий раз! Одна моя школьная подруга поступила в вуз лишь с третьей попытки, а другая, мечтавшая стать партработником, — с четвертой.\n" +
            "\n" +
            "Отстояв в длинных очередях и пройдя все необходимые процедуры оформления, положенные абитуриенту Московского университета, я поселилась в высотке — общежитии МГУ на Ленинских горах. В блоке, состоявшем из двух комнат, прихожей и санузла. Вместе со мной (без недели семнадцатилетней) в комнате оказалась уже явно не молодая (на мой тогдашний взгляд) абитуриентка.\n" +
            "\n" +
            "Как выяснилось, матерая газетчица из провинции, штурмовала журфак уже в пятый раз. Но теперь с твердой надеждой, потому что поступала, в отличие от меня, не на общих основаниях, а на основе результатов конкурса, предусмотренного для абитуриентов, отработавших по специальности не менее трех лет. Для таких «производственников» он был на порядок ниже, чем для школьников.\n" +
            "\n" +
            "Она смотрела на меня, салагу, то со снисхождением, то с сочувствием. Дескать, придется тебе сюда приехать еще не раз.\n" +
            "\n" +
            "А в другом блоке поселили девушку с четырехлетним абитуриентским стажем. И моя и без того некрепкая вера в успех становилась все слабее.\n" +
            "\n" +
            "Сочинение написала на «тройку», Харис — на «двойку», и решил забирать документы. Я же отправилась сдавать немецкий — и тоже только «удовлетворительно». Мало того, что уверенность была поколеблена соседками по общежитию, масла в огонь подлил и земляк, забравший документы. Решила, что бороться до конца не имеет смысла.\n" +
            "\n" +
            "Правда, как выяснилось потом, имеет. Так сделала одна из комкоров «Комсомольской искры» — сдала все экзамены, но, не набрав необходимых баллов на дневное отделение, с этим же количеством поступила на заочное. А проучившись год, перевелась на дневное.\n" +
            "\n" +
            "Забрав документы в деканате, я напоследок отправилась в кинотеатр «Октябрь» на Арбате. Там шла премьера фильма «Ватерлоо». И бывает же такое! В фойе встретила знакомую, Татьяну Цаплину, вместе с которой не один год играла в юношеской сборной области по баскетболу. Ныне это успешный нотариус Татьяна Юрьевна Григорьева, долгие годы возглавлявшая нотариальную палату Владимирской области. Она тоже не дошла до экзаменационного финиша и забрала документы с юрфака МГУ. Две бывшие абитуриентки пришли на мировую кинопремьеру, решив таким образом подсластить горькую пилюлю перед возвращением восвояси.\n" +
            "\n" +
            "Домой я приехала на электричке поздно вечером. Все уже спали. Ключа под дверью, как обычно, не обнаружила. Видимо, не ждали. Постучала, на стук вышла мама. «Не сдала», — сказала, опережая ее вопрос.\n" +
            "\n" +
            "А уже на следующий день отправилась с этой вестью в «Комсомольскую искру», которая дала мне рекомендацию для поступления. Конечно, волновало «общественное мнение». Что сказать знакомым, соседям, одноклассникам? «Ничего, — ответила Элла. — Друзьям эта правда — на печаль, недругам — на радость».\n" +
            "\n" +
            "Встретилась с редактором, Александром Ивановичем Кузнецовым. Он сразу же предложил работу по договору — «чтобы год не пропадал зря». Есть такая форма трудоустройства в газете: стаж идет, работа тоже. Формально ты не в штате, но в обойме. Обычно творческим сотрудникам положены оклад и гонорар. А «договорнику» — только гонорар. Что написал, точнее, что из написанного было опубликовано в газете, за то и получил: когда 25, когда 30, когда 40 рублей.\n" +
            "\n" +
            "Конечно, работая где-нибудь на заводе, я получала бы в два-три раза больше. И эти деньги оказались бы для семьи явно не лишними. Но родители поддержали. Такое сотрудничество с редакцией было очень ценным для понимания правильности выбора будущей профессии.\n" +
            "\n" +
            "Ну, а по вечерам, каждый день, исключая командировки, я ходила в областную библиотеку на улице Дзержинского, рядом с которой располагалась редакция. Надо было читать и перечитывать классику, заново учить ответы на вопросы, чтобы не забыть материал и подготовиться к поступлению через год.\n" +
            "\n" +
            "По мере приближения очередных вступительных экзаменов, я все больше склонялась к выбору журфака ЛГУ. Свою весомую лепту в это решение, кроме Юли Артемьевой, внес и папин брат Василий Николаевич, дядя Вася, служивший после войны под Ленинградом и полюбивший этот город.\n" +
            "\n" +
            "ПЕРВЫЙ ДЕНЬ В ЛЕНИНГРАДЕ\n" +
            "\n" +
            "С владимирского вокзала, еще того самого, одноэтажного, старинного, с лепниной, на который приезжал Ленин и который вскоре снесут и возведут на его месте нынешний — монументальное современное здание кубической формы, нас провожали на экзамены мамы.\n" +
            "\n" +
            "И на следующее утро три школьные подружки — Надя, Лена и я — вышли с поезда на Московском вокзале. А поскольку Надя и Лена в прошлом году уже поступали в Ленинградский педагогический институт имени Герцена на факультет археологии, то спрашивать, как добраться до Ленинградского университета, не пришлось.\n" +
            "\n" +
            "С вокзала мы прямиком отправились на Невский проспект образца 1972 года. Прежде с этой улицей я была знакома только по описанию Гоголя. Здесь нас подхватила и понесла мощная людская река. «Вынырнуть» из нее удалось дважды.\n" +
            "\n" +
            "Первый раз — чтобы сделать моментальную фотографию в автомате. Опустила монетки, задернула шторку, села лицом к зеркалу, что-то щелкнуло, осветив моментальной вспышкой, потом еще и еще, и из щелки поползла лента из нескольких неразрезанных фотографий формата побольше, чем для паспорта. Не откладывая в долгий ящик, одну я послала бабушке. Потом видела эту карточку у тети в альбоме. Две-три остались у меня. С них смотрит миловидная девочка, почти ребенок. Вот такой и встретил меня город Ленинград — колыбель трех революций.\n" +
            "\n" +
            "Второй раз мы отклонились от курса, зайдя в большой двухэтажный обувной магазин рядом с Дворцовой площадью. Как это ни покажется сегодня странным, но тогда меня поразил богатый выбор и красота обуви фабрики «Скороход». Очень понравились лодочки малинового цвета. Вскоре я их куплю — впервые самостоятельно, без мамы! Скороходовским туфлям не было сноса.\n" +
            "\n" +
            "Позже увижу этот величественный город в разную погоду. Чаще всего в капризную, когда можно ожидать всякого — утром солнце и тепло, а через час — ветер, тучи, дождь. Город, в котором даже в июле нельзя выходить из дома без зонта и теплой кофты. А в июне пляж на Детском острове, где еще вчера загорала и купалась, утром покроется снегом. Может, поэтому за пять лет учебы у меня никогда не возникнет мысли променять ветреный и малосолнечный Ленинград на родной Владимир? Но в тот первый день я увидела его солнечным, теплым и гостеприимным.\n" +
            "\n" +
            "ПОЛБАЛЛА\n" +
            "\n" +
            "Абитуриентов размещали в общежитии в Старом Петергофе. Изнывая от жары и неизвестности, мы с одноклассницами не могли усидеть в душном помещении, брали учебники и ехали в Новый Петергоф на Финский залив. А там пробирались по мелководью далеко в море, садились на камни и продолжали готовиться к экзаменам. Вскоре пришлось разделиться, поскольку у всех были разные экзамены, и в разные дни. Я покупала за 20 копеек входной билет на территорию дворцового комплекса и штудировала вопросы к билетам в одиночку.\n" +
            "\n" +
            "Когда надоедало долгое сидение на камнях или на лавочке, искала в знаменитом петродворцовом парке дальнюю аллею, где поменьше туристов, и ходила по ней из конца в конец, уткнувшись в книгу, целый день, до самого вечера. Кстати, эта система повторения материала в движении очень хорошо обостряла память.\n" +
            "\n" +
            "В общем, экзамены я сдала довольно успешно — три «четверки» и одна «пятерка». Самое главное, что за сочинение, на котором срезались многие абитуриенты, получила «четверку». И все-таки в списке поступивших моей фамилии не оказалось. Как выяснилось, не хватило всего полбалла! Было очень обидно. Но снова проходить через чистилище экзаменов я категорически не хотела. И на этот раз оказалась прагматичнее, чем год назад.\n" +
            "\n" +
            "Решила с этими баллами подавать документы на заочное отделение. Сразу же пошла на переговорный пункт и заказала разговор с родителями. После этого звонка в Ленинград срочно приехал папа. Но, как объяснили в деканате журфака, с такими баллами меня с удовольствием приняли бы на заочное отделение, если бы жила в северо-западной зоне, к которой относился Ленинградский университет. Такие тогда были правила.\n" +
            "\n" +
            "Получалось, все упирается в прописку. А в то время, наверное, легче было получить иностранное гражданство, чем прописаться в Москве или в Ленинграде. Единственный вариант, посоветовал заместитель декана Хасьби Сергеевич Булатцев, — найти такую работу, которая обеспечивала бы временной пропиской. Естественно, речь шла не о журналистике, а о тяжелом и непрестижном труде, которого чурались коренные ленинградцы. Специально для занятых черной работой выделялся лимит на временную регистрацию. То есть мне предстояло стать лимитчицей.\n" +
            "\n" +
            "Вместе с папой поехали на трамвае на окраину города. Вышли на конечной остановке. Это была Ржевка, где располагались военные склады. Вот их-то мне и предстояло охранять с ружьем в руках. В конторе ВОХР встретили начальника смены, пожилого узбека. Он объяснил режим работы: сутки — вахта, трое — выходной. Начальником отряда, в который меня определили, был финн, почти дедушка.\n" +
            "\n" +
            "Оказалось, что охраняют склады отнюдь не старики, а мои ровесники: студенты-заочники или вечерники Мухинки, Академии художеств, университета, не поступившие на дневное отделение или не имеющие возможности на нем учиться, а также готовящиеся к вступительным экзаменам в вузы Ленинграда на подготовительных курсах.\n" +
            "\n" +
            "ВОХР предоставляла общежитие. Нам его показали — одноэтажный кирпичный барак коридорного типа, расположенный на отшибе, с минимумом удобств и холодной водой в кране. Во второй половине августа было уже довольно прохладно, дело шло к осени. Решили, что будем искать квартиру поближе к центру. Ну, а приехать на Ржевку раз в три дня можно и на трамвае.\n" +
            "\n" +
            "ЛЬВИНЫЙ МОСТИК\n" +
            "\n" +
            "У пешеходного мостика, висящего над каналом Грибоедова, неподалеку от Юсуповского дворца, всегда собирается толпа молодых людей. Четыре чугунных льва наблюдают за ней с равнодушным видом. Действительно, чему удивляться, ведь ничего не происходит!\n" +
            "\n" +
            "Но стоит появиться здесь хозяину сдаваемой в наем жилплощади, как вся толпа, услышав заветные слова, устремляется в его сторону. «Девочки? Не нужны! — заявляет мрачная старуха. — Будут водить мальчиков». Мальчики с воодушевлением начинают теснить девочек. «Мальчики не нужны, — продолжает привереда. — Будут водить девочек».\n" +
            "\n" +
            "«Так кто же вам нужен?» — ропщет толпа.\n" +
            "\n" +
            "Можно было ходить на Львиный мостик каждый день, как на работу — и безрезультатно. Но все же кому-то изредка везло. Вот как нам с папой. Но лучше бы не было такого везения!\n" +
            "\n" +
            "Блондинка лет сорока с перманентной завивкой подошла и сказала, что ей срочно нужно уехать к мужу-капитану на Север, поэтому она сдает на полгода комнату в районе станции метро «Елизаровская». Деньги — вперед. Вместе с ней мы отправились на смотрины. Комната нас устроила. Получив свои 200 рублей, хозяйка торопливо отдала ключи. Я проводила папу и прямо с Московского вокзала поехала заселяться. Но на пороге меня встретила незнакомая дама — как выяснилось, соседка блондинки: «Ничего не знаю, я вам здесь жить не позволю, верните ключи!»\n" +
            "\n" +
            "То, что произошло с нами, сегодня назвали бы словом «развод». Тогда же дремучая провинциальная доверчивость не допускала мысли, что мы попали в лапы к аферисткам. Папа даже наказал мне написать командованию части, в которой якобы служил муж хозяйки. Письмо, изобличающее жену морского офицера, я отправила, но ответа, конечно, не получила. А горькая правда дошла до нас через много лет, уже в эпоху разгула капитализма, когда подобные аферы стали обычным делом. Мы с папой даже посмеялись над своей наивностью.\n" +
            "\n" +
            "На этом совместные поиски квартиры и закончились. Мы потерпели фиаско. Папа возвратился во Владимир. А я поселилась в общежитии на Ржевке. Не ночевать же на вокзале! Но надежда снять квартиру ближе к центру не оставляла, тем более что занятия в университете уже начались. А я поставила перед собой цель за один год экстерном закончить два курса на заочном отделении, а затем попытаться перевестись на дневное обучение. Эта возможность открывалась при условии сдачи на «отлично» всех экзаменов и с «понижением» на один курс. Таким образом, время не было бы потеряно, и я смогла бы продолжить учебу вместе с теми, с кем сдавала вступительные экзамены.\n" +
            "\n" +
            "Общежитское житье, холодное, неуютное, казарменное, продолжалось недолго. Вместе с соседкой по комнате на Ржевке, Наташей Щербаковой из Новороссийска, поступавшей на журфак и оказавшейся точно в такой же ситуации, как и я, решили искать комнату на двоих.\n" +
            "\n" +
            "И снова пришлось идти на Львиный мостик. Попытка была не вполне удачной. Комната в Фонарном переулке, неподалеку от Исаакия, сдавалась на месяц — на время, пока хозяйка жила на даче, и только одному постояльцу. Мы с Наташей были вынуждены допоздна сидеть на лавочке в сквере перед собором, чтобы проникнуть в квартиру под покровом ночи, не привлекая внимания соседки.\n" +
            "\n" +
            "Бесплатный фрагмент закончился.\n" +
            "Купите книгу, чтобы продолжить чтение.";
}
