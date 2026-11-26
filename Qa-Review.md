# Kodun sadələşdirilməsi üzrə review

Məqsəd kodun müəllifini müəyyənləşdirmək deyil: kodun AI ilə yazıldığını təkcə görünüşünə görə demək olmaz. Burada komandanın daha rahat izah edə biləcəyi konkret sadələşdirmə namizədləri qeyd edilib. Mənbə kodu dəyişdirilməyib. Sətirlər hazırkı versiyaya aiddir.

## Əvvəlcə edilə bilənlər

1. **Köhnə kod bloklarını təmizləmək — aşağı risk.** `core/src/main/java/entities/NoVacancyGame.java:1`–`94` aktiv sinfin əvvəlki versiyasının bütöv şərhə alınmış surətidir. `core/src/main/java/entities/Player.java:152`–`156` və `:200`–`207` də köhnə hərəkət/toqquşma kodudur. Bunları silmək işləyən kodu dəyişmir; tarixçəyə ehtiyac varsa Git-də qalır. Silməzdən əvvəl komandanın saxlamaq istədiyi qeydləri yoxlayın.

2. **Uzun və özünü təkrarlayan şərhləri qısaltmaq — aşağı risk.** Məsələn, `entities/Enemy.java:39`, `:59`, `:94`, `:122` və `entities/Main.java:19`, `:77`, `:141` ətrafında sadə əməliyyatlara aid uzun Javadoc blokları var. Bir-iki cümlə ilə məqsədi yazmaq, xüsusilə qeyri-aşkar səbəbləri (məsələn, teksturanın kim tərəfindən azad edilməsi və kiçik hərəkət addımları) saxlamaq kifayətdir. Müəllif qeydlərini komandanın istəyinə uyğun qoruyun.

3. **`Main.render` metodunu mərhələlərə ayırmaq — orta risk.** `entities/Main.java:156`–`250` giriş, pauza, düşmənlər, tələlər, əşyalar, kamera və çəkməni bir yerdə edir. Eyni sinifdə `updateEnemies(dt)`, `updateTraps(dt)`, `collectItems()` və `drawWorld()` kimi bir neçə sadə metodla ardıcıllığı görünən etmək olar. Açar və iksirin `dx`, `dy`, məsafə yoxlamaları (`:192`–`209`) üçün kiçik ortaq məsafə metodu təkrarı azaldar; iksirin yalnız can az olanda götürülməsi qorunmalıdır. Pauza, yenidən başlama və sətir üzrə çəkmə sırası dəyişməməlidir.

4. **Oyunçunun hərəkətini oxunaqlı mərhələlərə bölmək — orta risk.** `entities/Player.java:114`–`150` düymələri, istiqaməti, animasiyanı, səsi və toqquşmanı birləşdirir. Giriş/istiqamət və hərəkət/toqquşma üçün sadə adlı metodlar ayırmaq mümkündür. `dx`/`dy` hesabını adi `if`-lərlə yazmaq üçlü operatorları izah etməyi də asanlaşdıra bilər. Diaqonal sürətin normallaşdırılmasını, 8 piksellik kiçik addımları və oxlar üzrə ayrıca toqquşmanı saxlayın: bunlar divardan keçmənin və divar boyunca sürüşmənin davranışına təsir edir.

## Daha diqqətlə baxılacaq hissələr

5. **Menyu girişini iki yerə ayırmaq — orta risk.** `entities/menu/UiScreen.java:307`–`355` siçan, sürüşdürmə, klaviatura və geri qayıtmanı bir metodda idarə edir. Eyni sinifdə `handleMouse()` və `handleKeyboard()` kimi iki metod izahı asanlaşdıra bilər. Widget aktivləşdirildikdə erkən `return`, yalnız siçan hərəkət edəndə fokusun dəyişməsi, sürüşdürmənin buraxılması və deaktiv elementlərin ötürülməsi saxlanmalıdır. Bütün menyularda siçan, klaviatura və səs slayderlərini yoxlamaq lazımdır.

6. **Xəritə obyektlərinin oxunmasını ayırmaq — orta risk.** `entities/Maze.java:86`–`140` həm xəritəni yükləyir, həm də tələ, düşmən, açar, oyunçu və iksir mövqelərini oxuyur. Obyekt dövrünü `loadObjects()` kimi ayrıca metoda çıxarmaq konstruktoru qısaldar. Tələnin düzbucaqlı olması yoxlamasını, `player_spawn` adının ayrıca yoxlanmasını və xəta zamanı log/`loaded` davranışını qoruyun.

7. **Divarların sətir-sətir çəkilməsi — yüksək risk, əvvəl araşdırılmalıdır.** `entities/Maze.java:271`–`328` 20 elementli vertex massivini və çevrilmə/döndərmə üçün indeks dəyişmələrini əl ilə idarə edir. Bu, izahı çətin hissədir; libGDX-in uyğun çəkmə imkanları ilə daha aydın variantın mümkünlüyü araşdırıla bilər. Bununla belə, `Main.render` obyektləri divar sətirləri ilə növbələşdirir; sadəcə bütün divar qatını bir dəfəyə çəkmək personajların ön/arxa görünüşünü dəyişər. Döndərilmiş və çevrilmiş tile-lar, böyük tile ölçüləri və animasiyalar da müqayisə edilmədən kodu ixtisar etməyin.

8. **İki oyun obyektləri dəstini dəqiqləşdirmək — əvvəl istifadəni yoxlayın.** `entities/Player.java` və `entities/Enemy.java` ilə yanaşı `core/src/main/java/io/github/conditionexit/novacancy/Player.java` və `Enemy.java` da var. Eyni adlar oxucunu çaşdıra bilər. Hazırkı oyun ekranı `entities` siniflərindən istifadə edir; digər dəstin nəzərdə tutulmuş gələcək iş və ya başqa giriş nöqtəsi olub-olmadığını komanda və bütün istifadə yerləri ilə yoxlayın. Yalnız artıq olduğu təsdiqlənəndən sonra silmək və ya adlandırmanı aydınlaşdırmaq olar.

## Sadələşdirmə zamanı qorunmalı olanlar

- `NoVacancyGame.java:205`–`219` ekran keçidini növbəti kadra saxlayır və köhnə ekranı azad edir. Bunu yalnız qısa görünsün deyə birbaşa keçidlə əvəz etmək resursların işlək render zamanı azad edilməsinə səbəb ola bilər.
- `lwjgl3/.../StartupHelper.java` platforma başlanğıcı ilə əlaqəlidir. Uzun görünməsi təkbaşına silinməsi üçün əsas deyil; dəstəklənən platformalarda yoxlamadan dəyişməyin.
- `Key` və `Potion` oxşardır, amma hazırda hər biri kiçikdir. Onları mütləq abstrakt sinif və ya mürəkkəb iyerarxiyaya çevirmək lazım deyil. `Potion.isConcumed()` adı ayrıca, bütün çağırışları ilə birlikdə `isConsumed()` kimi düzəldilə bilər.

## Yoxlama planı

Hər dəyişikliyi ayrıca edin və layihəni yığın. Oyunda menyu/ayarlar, klaviatura və siçan, səviyyəyə keçid, pauza və restart, divara və düşmənə toxunma, tələ, açar, iksir, səs və xəritədə personajın divarın önündə/arxasında görünməsini yoxlayın. Böyük və kiçik kadr müddətində hərəkəti də yoxlayın. Bu sənəd statik kod baxışıdır; oyun işə salınmayıb və bütün mümkün sadələşdirmələrin siyahısı olduğu iddia edilmir.
