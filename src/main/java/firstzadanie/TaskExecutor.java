package firstzadanie;

import java.util.concurrent.Phaser;

public class TaskExecutor implements Runnable {
    private final int threadId;
    private final Phaser phaser;
    private final double[] data;
    private final int startIndex;
    private final int endIndex;
    private double partialSum;
    private static double totalSum = 0;
    private static final Object lock = new Object();

    // Приватный конструктор для Builder
    private TaskExecutor(Builder builder) {
        this.threadId = builder.threadId;
        this.phaser = builder.phaser;
        this.data = builder.data;
        this.startIndex = builder.startIndex;
        this.endIndex = builder.endIndex;
        this.partialSum = 0;

        // Регистрируем поток в Phaser
        if (this.phaser != null) {
            this.phaser.register();
        }
    }

    // Статический внутренний класс Builder
    public static class Builder {
        private int threadId;
        private Phaser phaser;
        private double[] data;
        private int startIndex;
        private int endIndex;

        public Builder() {}

        public Builder threadId(int threadId) {
            this.threadId = threadId;
            return this;
        }

        public Builder phaser(Phaser phaser) {
            this.phaser = phaser;
            return this;
        }

        public Builder data(double[] data) {
            this.data = data;
            return this;
        }

        public Builder startIndex(int startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public Builder endIndex(int endIndex) {
            this.endIndex = endIndex;
            return this;
        }

        public TaskExecutor build() {
            // Валидация параметров
            if (phaser == null) {
                throw new IllegalStateException("Phaser не может быть null");
            }
            if (data == null) {
                throw new IllegalStateException("Данные не могут быть null");
            }
            if (startIndex < 0 || endIndex > data.length || startIndex >= endIndex) {
                throw new IllegalStateException("Некорректные индексы: startIndex=" + startIndex + ", endIndex=" + endIndex);
            }

            return new TaskExecutor(this);
        }
    }

    @Override
    public void run() {
        try {
            // Фаза 1: Чтение и подготовка данных
            System.out.println("Поток " + threadId + " начал Фазу 1 (подготовка данных). Диапазон: ["
                    + startIndex + " - " + (endIndex - 1) + "]");

            // Имитация подготовки данных
            Thread.sleep(100);

            System.out.println("Поток " + threadId + " завершил Фазу 1");
            phaser.arriveAndAwaitAdvance(); // Ожидание завершения фазы 1 всеми потоками

            // Фаза 2: Вычисление квадратов чисел
            System.out.println("Поток " + threadId + " начал Фазу 2 (вычисление квадратов)");

            for (int i = startIndex; i < endIndex; i++) {
                partialSum += data[i] * data[i];
            }

            System.out.println("Поток " + threadId + " завершил Фазу 2. Частичная сумма: " + partialSum);
            phaser.arriveAndAwaitAdvance(); // Ожидание завершения фазы 2 всеми потоками

            // Фаза 3: Сбор частичных сумм
            System.out.println("Поток " + threadId + " начал Фазу 3 (сбор результатов)");

            synchronized (lock) {
                totalSum += partialSum;
                System.out.println("Поток " + threadId + " добавил свою сумму. Общая сумма: " + totalSum);
            }

            System.out.println("Поток " + threadId + " завершил Фазу 3");
            phaser.arriveAndAwaitAdvance(); // Ожидание завершения фазы 3 всеми потоками

            // Фаза 4: Проверка результатов (дополнительная фаза)
            if (threadId == 0) { // Только первый поток выводит финальный результат
                System.out.println("Общая сумма квадратов всех элементов: " + totalSum);

                // Проверка правильности
                double checkSum = 0;
                for (int i = 0; i < Math.min(10, data.length); i++) {
                    checkSum += data[i] * data[i];
                }
                System.out.println("Проверка (первые 10 элементов): " + checkSum);
            }

            phaser.arriveAndDeregister(); // Завершение участия потока

        } catch (InterruptedException e) {
            System.err.println("Поток " + threadId + " был прерван: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Ошибка в потоке " + threadId + ": " + e.getMessage());
        }
    }

    public static double getTotalSum() {
        return totalSum;
    }

    public static void resetTotalSum() {
        totalSum = 0;
    }
}
