package firstzadanie;

import java.io.IOException;
import java.util.concurrent.Phaser;

public class MainPhaser {
    public static void main(String[] args) {
        System.out.println("ЗАДАНИЕ 1: Многократная синхронизация фаз с помощью Phaser\n");

        String filename = "numbers.bin";
        int numCount = 1_000_000; // 1 миллион чисел
        int numThreads = 4; // Количество потоков

        try {
            // Генерируем файл с данными
            System.out.println("Генерация файла с " + numCount + " числами...");
            DataGenerator.generateBinaryFile(filename, numCount);

            // Читаем данные из файла
            System.out.println("Чтение данных из файла...");
            double[] data = DataGenerator.readBinaryFile(filename);
            System.out.println("Прочитано " + data.length + " чисел\n");

            // Сброс предыдущей суммы
            TaskExecutor.resetTotalSum();

            // Создаем Phaser с начальным количеством сторон = 1 (основной поток)
            Phaser phaser = new Phaser(1);

            // Вычисляем размер блока для каждого потока
            int blockSize = data.length / numThreads;

            // Создаем и запускаем потоки
            Thread[] threads = new Thread[numThreads];

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < numThreads; i++) {
                int startIndex = i * blockSize;
                int endIndex = (i == numThreads - 1) ? data.length : (i + 1) * blockSize;

                // Использование Builder для создания задачи
                TaskExecutor task = new TaskExecutor.Builder()
                        .threadId(i)
                        .phaser(phaser)
                        .data(data)
                        .startIndex(startIndex)
                        .endIndex(endIndex)
                        .build();

                threads[i] = new Thread(task);
                threads[i].start();
            }

            // Основной поток ждет завершения всех фаз
            phaser.arriveAndAwaitAdvance(); // Завершение фазы регистрации

            int phase = phaser.getPhase();
            System.out.println("\nПереход к фазе " + (phase + 1) + " \n");

            // Ждем завершения всех фаз
            while (phaser.getRegisteredParties() > 1) {
                phaser.arriveAndAwaitAdvance();
                if (phaser.getPhase() < 3) { // Не выводим для последней фазы
                    System.out.println("\nПереход к фазе " + (phaser.getPhase() + 1) + "\n");
                }
            }

            long endTime = System.currentTimeMillis();

            // Ждем завершения всех потоков
            for (Thread thread : threads) {
                thread.join();
            }

            System.out.println("Время выполнения: " + (endTime - startTime) + " мс");
            System.out.println("Количество потоков: " + numThreads);
            System.out.println("Количество чисел: " + data.length);
            System.out.println("Финальная сумма квадратов: " + TaskExecutor.getTotalSum());

            // Проверка правильности
            double checkSum = 0;
            for (double num : data) {
                checkSum += num * num;
            }
            System.out.println("Проверка (последовательное вычисление): " + checkSum);
            System.out.println("Результат корректен: " + (Math.abs(TaskExecutor.getTotalSum() - checkSum) < 0.0001));

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
