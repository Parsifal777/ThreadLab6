package secondadanie;

import java.util.concurrent.Exchanger;

public class MainExchanger {
    public static void main(String[] args) {
        System.out.println("ЗАДАНИЕ 2: Обмен данных между потоками с помощью Exchanger\n");

        // Параметры
        int arraySize = 20; // Размер массива для наглядности
        int iterations = 5; // Количество итераций обмена

        // Создаем Exchanger для обмена массивами
        Exchanger<int[]> exchanger = new Exchanger<>();

        System.out.println("Параметры эксперимента:");
        System.out.println("- Размер массива: " + arraySize);
        System.out.println("- Количество итераций обмена: " + iterations);
        System.out.println("- Тип данных: целые числа");

        // Создаем потоки с использованием Builder
        GeneratorThread generator = new GeneratorThread.Builder()
                .exchanger(exchanger)
                .threadId(1)
                .arraySize(arraySize)
                .iterations(iterations)
                .verbose(true)
                .delayMs(500)
                .build();

        TransformerThread transformer = new TransformerThread.Builder()
                .exchanger(exchanger)
                .threadId(1)
                .iterations(iterations)
                .verbose(true)
                .delayMs(500)
                .measurePerformance(true)
                .build();

        Thread generatorThread = new Thread(generator, "Generator-Thread");
        Thread transformerThread = new Thread(transformer, "Transformer-Thread");

        long startTime = System.currentTimeMillis();

        // Запускаем потоки
        System.out.println("\nЗапуск потоков...\n");
        generatorThread.start();
        transformerThread.start();

        try {
            // Ожидаем завершения потоков
            generatorThread.join();
            transformerThread.join();

            long endTime = System.currentTimeMillis();

            System.out.println("Время выполнения: " + (endTime - startTime) + " мс");
            System.out.println("Количество успешных обменов: " + iterations);

            // Демонстрация гибкости Builder
            System.out.println("С помощью Builder можно легко создавать потоки с разными параметрами:");

            // Создаем конфигурацию для быстрого тестирования
            GeneratorThread fastGenerator = new GeneratorThread.Builder()
                    .exchanger(exchanger)
                    .threadId(2)
                    .arraySize(10)
                    .iterations(3)
                    .verbose(false)  // Тихий режим
                    .delayMs(100)     // Маленькая задержка
                    .build();

            System.out.println("- Быстрый генератор: arraySize=10, iterations=3, delay=100ms");

            // Создаем конфигурацию с измерением производительности
            TransformerThread perfTransformer = new TransformerThread.Builder()
                    .exchanger(exchanger)
                    .threadId(2)
                    .iterations(10)
                    .verbose(false)
                    .delayMs(0)
                    .measurePerformance(true)
                    .build();

            System.out.println("- Преобразователь с измерением: iterations=10, delay=0ms");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
