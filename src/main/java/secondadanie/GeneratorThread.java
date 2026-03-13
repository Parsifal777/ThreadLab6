package secondadanie;

import java.util.Random;
import java.util.concurrent.Exchanger;

public class GeneratorThread implements Runnable {
    private final Exchanger<int[]> exchanger;
    private final int threadId;
    private final int arraySize;
    private final int iterations;
    private final boolean verbose;
    private final long delayMs;

    // Приватный конструктор для Builder
    private GeneratorThread(Builder builder) {
        this.exchanger = builder.exchanger;
        this.threadId = builder.threadId;
        this.arraySize = builder.arraySize;
        this.iterations = builder.iterations;
        this.verbose = builder.verbose;
        this.delayMs = builder.delayMs;
    }

    // Статический внутренний класс Builder
    public static class Builder {
        private Exchanger<int[]> exchanger;
        private int threadId = 1;
        private int arraySize = 20;
        private int iterations = 5;
        private boolean verbose = true;
        private long delayMs = 500;

        public Builder exchanger(Exchanger<int[]> exchanger) {
            this.exchanger = exchanger;
            return this;
        }

        public Builder threadId(int threadId) {
            this.threadId = threadId;
            return this;
        }

        public Builder arraySize(int arraySize) {
            this.arraySize = arraySize;
            return this;
        }

        public Builder iterations(int iterations) {
            this.iterations = iterations;
            return this;
        }

        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public Builder delayMs(long delayMs) {
            this.delayMs = delayMs;
            return this;
        }

        public GeneratorThread build() {
            if (exchanger == null) {
                throw new IllegalStateException("Exchanger не может быть null");
            }
            if (arraySize <= 0) {
                throw new IllegalStateException("Размер массива должен быть положительным");
            }
            if (iterations <= 0) {
                throw new IllegalStateException("Количество итераций должно быть положительным");
            }

            return new GeneratorThread(this);
        }
    }

    private void log(String message) {
        if (verbose) {
            System.out.println(message);
        }
    }

    @Override
    public void run() {
        Random random = new Random();
        int[] data = new int[arraySize];

        try {
            for (int iter = 0; iter < iterations; iter++) {
                log("\n[Генератор " + threadId + "] Итерация " + (iter + 1));

                // Генерируем новый массив
                if (iter == 0) {
                    // Первая итерация - генерируем случайные числа
                    for (int i = 0; i < arraySize; i++) {
                        data[i] = random.nextInt(1000);
                    }
                    log("[Генератор " + threadId + "] Сгенерирован новый массив");
                } else {
                    // На следующих итерациях модифицируем полученный отсортированный массив
                    log("[Генератор " + threadId + "] Получен отсортированный массив от преобразователя");

                    // Добавляем случайные числа к отсортированному массиву для новой генерации
                    for (int i = 0; i < arraySize; i++) {
                        data[i] += random.nextInt(100);
                    }
                    log("[Генератор " + threadId + "] Модифицирован массив на основе предыдущих результатов");
                }

                // Выводим первые 10 элементов для демонстрации
                if (verbose) {
                    System.out.print("[Генератор " + threadId + "] Первые 10 элементов: ");
                    for (int i = 0; i < Math.min(10, arraySize); i++) {
                        System.out.print(data[i] + " ");
                    }
                    System.out.println();
                }

                // Отправляем массив преобразователю
                log("[Генератор " + threadId + "] Отправка массива преобразователю...");
                data = exchanger.exchange(data);

                if (data == null) {
                    log("[Генератор " + threadId + "] ОШИБКА: Получен null массив!");
                    // Создаем новый массив для продолжения
                    data = new int[arraySize];
                    for (int i = 0; i < arraySize; i++) {
                        data[i] = random.nextInt(1000);
                    }
                }

                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            log("\n[Генератор " + threadId + "] Завершил работу");

        } catch (InterruptedException e) {
            System.err.println("[Генератор " + threadId + "] Прерван: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
