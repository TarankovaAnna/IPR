package animals;

import food.Food;

public abstract class Herbivore extends Animal {


    public int getSatiety() {

        return super.getSatiety();
    }

    @Override
    public void eat(Food food) {

    }
}
