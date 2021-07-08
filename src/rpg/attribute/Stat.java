package rpg.attribute;

public class Stat {
    public int strength;
    public int vitality;
    public int agility;
    public int sensibility;
    public int mentality;
    public int point;

    public Stat() {
        strength = 0;
        vitality = 0;
        agility = 0;
        sensibility = 0;
        mentality = 0;
        point = 0;
    }

    public Stat(int strength, int vitality, int agility, int sensibility, int mentality) {
        this.strength = strength;
        this.vitality = vitality;
        this.agility = agility;
        this.sensibility = sensibility;
        this.mentality = mentality;
        this.point = 0;
    }
}
