package rpg.move;

public enum MoveType {
    DEFAULT("기본", "항상 이동속도를 적용합니다."),
    SPRINT_ONLY("달리기시만 적용", "달리기 상태일 때만 이동속도의 빠르기를 적용합니다."),
    HALF("반감", "이동속도의 빠르기를 50%만 적용받습니다."),
    NEVER("미적용", "최대 이동속도를 기본(100)으로 설정합니다."),
    BALANCE("균형 조정", "걸을 떄와 달릴 때의 이동속도를 균형 조정합니다.");

    private final String name;
    private final String description;
    MoveType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
}
