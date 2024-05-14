package animals;

import food.Food;
import food.Meat;

import java.util.Random;

public class Kotik extends Animal implements Run, Swim, Voice{
    private String name;
    private String voice;
    private int satiety;
    private int weight;



    private static int count = 0;
    private static final int METHODS = 5;

    // Конструкторы
    public Kotik(String name, String voice, int satiety, int weight) {
        this.name = name;
        this.voice = voice;
        this.satiety = satiety;
        this.weight = weight;
        count++;
    }

    public Kotik() {
        count++;
    }

    public static int getCount() {
        return count;
    }


    public void eat(int amountOfSatiety) {
        satiety = satiety + amountOfSatiety;
    }

    public void eat(int amountOfSatiety, String nameOfEat) {
        satiety = satiety + amountOfSatiety;
    }

    public void eat() {
        eat(10, "meet");
        System.out.println("ел");
    }


    public boolean play() {
        if (satiety > 0) {
            System.out.println("играл");
            satiety--;
        } else {
            System.out.println("Котик просит есть");
            return false;
        }
        return true;
    }

    public boolean hunt() {
        if (satiety > 0) {
            System.out.println("охотился");
            satiety--;

        } else {
            System.out.println("Котик просит есть");
            return false;
        }
        return true;
    }

    public boolean sleep() {
        if (satiety > 0) {
            System.out.println("спал");
            satiety--;

        } else {
            System.out.println("Котик просит есть");
            return false;
        }
        return true;
    }

    public boolean wash() {
        if (satiety > 0) {
            System.out.println("мылся");
            satiety--;

        } else {
            System.out.println("Котик просит есть");
            return false;
        }
        return true;
    }

    public boolean walk() {
        if (satiety > 0) {
            System.out.println("гулял");
            satiety--;

        } else {
            System.out.println("Котик просит есть");
            return false;
        }
        return true;
    }

    public String[] liveAnotherDay() {
        String[] day = new String[24];
        for (int i = 0; i < 24; i++) {
            // day[i] = String.valueOf(i); // Час
            switch ((int) (Math.random() * METHODS) + 1) {
                case 1:
                    if (satiety <= 0) {
                        eat();
                        day[i] = i + " - ел";
                    } else {
                        play();
                        day[i] = i + " - играл";
                    }
                    break;
                case 2:
                    if (satiety <= 0) {
                        eat();
                        day[i] = i + " - ел";
                    } else {
                        sleep();
                        day[i] = i + " - спал";
                    }
                    break;
                case 3:
                    if (satiety <= 0) {
                        eat();
                        day[i] = i + " - ел";
                    } else {
                        wash();
                        day[i] = i + " - умывался";
                    }
                    break;
                case 4:
                    if (satiety <= 0) {
                        eat();
                        day[i] = i + " - ел";
                    } else {
                        walk();
                        day[i] = i + " - гулял";
                    }
                    break;
                case 5:
                    if (satiety <= 0) {
                        eat();
                        day[i] = i + " - ел";
                    } else {
                        hunt();
                        day[i] = i + " - охотился";
                    }
                    break;
            }
        }
        return day;
    }

    public int getSatiety() {

        return satiety;
    }

    @Override
    public void eat(Food food) {
        if (food instanceof Meat) {
            System.out.println("Рабочий кормит котика мясом");
            System.out.println("Котик ест мясо");
        } else {
            System.out.println("Рабочий кормит котика травой");
            System.out.println("Котик не ест траву!");
        }
    }


    public String getName() {
        return name;
    }

    //public String getVoice() {
   //     return voice;
  //  }

    public double getWeight() {
        return weight;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public void setSatiety(int satiety) {
        this.satiety = satiety;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public void run() {
        System.out.println("Котик бегает");
    }

    @Override
    public void swim() {
        System.out.println("Котик плавает");
    }

    @Override
    public String getVoice() {
        return "Котик говорит \"Мяу\"";
    }

}