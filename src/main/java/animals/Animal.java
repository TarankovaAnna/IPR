package animals;

import food.Food;

public abstract class Animal {
    public void setSatiety(int satiety) {
        this.satiety = satiety;
    }

    private int satiety;
    public int getSatiety() {

        return satiety;
    }

    public abstract void eat(Food food);



}
