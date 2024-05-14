package animals;

import food.Food;

public abstract class Carnivorous extends Animal{


    @Override
    public void eat(Food food) {

    }
    public int getSatiety() {

        return super.getSatiety();
    }

}
