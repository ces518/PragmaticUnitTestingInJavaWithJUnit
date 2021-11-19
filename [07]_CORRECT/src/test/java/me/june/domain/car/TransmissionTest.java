package me.june.domain.car;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 사전조건 : 자동차가 달릴수 있는 상태가 되어야함
 * - 기어를 파킹 상태로 놓으려면 반드시 정지상태어야 한다.
 * 사전조건이 맞지 않은 상태에서 파킹 상태로 요청할 경우 -> 무시해야함 (우아하게 동작하기 위함)
 *
 * 사후조건 : 코드가 참을 유지해야 하는 조건
 * - 사이드이펙트를 검사해야할 경우도 있다.
 * - brakeToStop 을 호출하면 속도가 0으로 지정되는 사이드이펙트
 *
 */
class TransmissionTest {

    Transmission transmission;
    Car car;

    @BeforeEach
    void setUp() {
        car = new Car();
        transmission = new Transmission(car);
    }

    /**
     * 가속후에는 기어가 계속 "드라이브" 이다.
     */
    @Test
    void remainsInDriverAfterAcceleration() {
        transmission.shift(Gear.DRIVE);
        car.accelerateTo(35);
        assertEquals(Gear.DRIVE, transmission.getGear());
    }

    /**
     * 운전중엔 기어를 "파킹" 으로 놓을 수 없다.
     */
    @Test
    void ignoreShiftToParkWhileInDrive() {
        transmission.shift(Gear.DRIVE);
        car.accelerateTo(30);

        transmission.shift(Gear.PARK);

        assertEquals(Gear.DRIVE, transmission.getGear());
    }

    /**
     * 브레이크를 밟은 후 기어를 "파킹" 으로 놓을 수 있다.
     */
    @Test
    void allowsShiftToParkWhenNotMoving() {
        transmission.shift(Gear.DRIVE);
        car.accelerateTo(30);
        car.brakeToStop();

        transmission.shift(Gear.PARK);

        assertEquals(Gear.PARK, transmission.getGear());
    }
}