package secondadanie;

import java.util.Arrays;
import java.util.concurrent.Exchanger;

public class TransformerThread implements Runnable {
    private final Exchanger<int[]> exchanger;
    private final int threadId;
    private final int iterations;
    private final boolean verbose;
    private final long delayMs;
    private final boolean measurePerformance;

    // Приватный конструктор для Builder
    private TransformerThread(Builder builder) {
        this.exchanger = builder.exchanger;
        this.threadId = builder.threadId;
        this.iterations = builder.iterations;
        this.verbose = builder.verbose;
        this.delayMs = builder.delayMs;
        this.measurePerformance = builder.measurePerformance;
    }

    // Статический внутренний класс Builder
    public static class Builder {
        private Exchanger<int[]> exchanger;
        private int threadId = 1;
        private int iterations = 5;
        private boolean verbose = true;
        private long delayMs = 500;
        private boolean measurePerformance = true;

        public Builder exchanger(Exchanger<int[]> exchanger) {
            this.exchanger = exchanger;
            return this;
        }

        public Builder threadId(int threadId) {
            this.threadId = threadId;
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

        public Builder measurePerformance(boolean measurePerformance) {
            this.measurePerformance = measurePerformance;
            return this;
        }

        public TransformerThread build() {
            if (exchanger == null) {
                throw new IllegalStateException("Exchanger не может быть null");
            }
            if (iterations <= 0) {
                throw new IllegalStateException("Количество итераций должно быть положительным");
            }

            return new TransformerThread(this);
        }
    }

    private void log(String message) {
        if (verbose) {
            System.out.println(message);
        }
    }

    @Override
    public void run() {
        long totalSortTime = 0;
        int[] processedArray = null;

        try {
            for (int iter = 0; iter < iterations; iter++) {
                log("\n[Преобразователь " + threadId + "] Итерация " + (iter + 1));

                // Получаем массив от генератора
                log("[Преобразователь " + threadId + "] Ожидание массива от генератора...");

                int[] receivedArray;
                if (iter == 0) {
                    // Первая итерация - ожидаем массив от генератора, отправляем null
                    receivedArray = exchanger.exchange(null);
                } else {
                    // Последующие итерации - отправляем обработанный массив и получаем новый
                    receivedArray = exchanger.exchange(processedArray);
                }

                // Проверяем, что получили массив
                if (receivedArray == null) {
                    log("[Преобразователь " + threadId + "] ОШИБКА: Получен null массив! Пропускаем итерацию");
                    continue;
                }

                log("[Преобразователь " + threadId + "] Получен массив размером " + receivedArray.length);

                // Выводим первые 10 элементов полученного массива
                if (verbose) {
                    System.out.print("[Преобразователь " + threadId + "] Полученные первые 10 элементов: ");
                    for (int i = 0; i < Math.min(10, receivedArray.length); i++) {
                        System.out.print(receivedArray[i] + " ");
                    }
                    System.out.println();
                }

                // Сортируем массив
                log("[Преобразователь " + threadId + "] Сортировка массива...");

                long sortStartTime = 0;
                if (measurePerformance) {
                    sortStartTime = System.nanoTime();
                }

                Arrays.sort(receivedArray);
                processedArray = receivedArray; // Сохраняем обработанный массив для следующей итерации

                if (measurePerformance) {
                    long sortEndTime = System.nanoTime();
                    long sortTimeMs = (sortEndTime - sortStartTime) / 1_000_000;
                    totalSortTime += sortTimeMs;
                    if (sortTimeMs > 0) {
                        log("[Преобразователь " + threadId + "] Сортировка завершена за " + sortTimeMs + " мс");
                    } else {
                        log("[Преобразователь " + threadId + "] Сортировка завершена (менее 1 мс)");
                    }
                }

                // Выводим первые 10 элементов отсортированного массива
                if (verbose) {
                    System.out.print("[Преобразователь " + threadId + "] Отсортированные первые 10 элементов: ");
                    for (int i = 0; i < Math.min(10, receivedArray.length); i++) {
                        System.out.print(receivedArray[i] + " ");
                    }
                    System.out.println();
                }

                log("[Преобразователь " + threadId + "] Массив отсортирован и готов к следующему обмену");

                // Задержка для наглядности
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            if (measurePerformance && totalSortTime > 0) {
                log("\n[Преобразователь " + threadId + "] Среднее время сортировки: "
                        + (totalSortTime / iterations) + " мс");
            }

            log("\n[Преобразователь " + threadId + "] Завершил работу");

        } catch (InterruptedException e) {
            System.err.println("[Преобразователь " + threadId + "] Прерван: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
