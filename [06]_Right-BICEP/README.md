# 6장 Right-BICEP
- Right-BICEP 은 무엇을 테스트할지에 대해 쉽게 선별하게 해준다.
- Right : 결과가 올바른가 ?
- B : 경계조건을 맞는가 ?
- I : 역 검사를 할 수 있는가 ?
- C : 다른 수단을 활용해 교차검사를 활 수 있는가 ?
- E : 오류 조건을 강제로 발생시킬 수 있는가 ?
- P : 성능 조건은 기준에 부합한가 ?

## Right : 결과가 올바른가 ?
- 테스트 코드는 무엇보다 먼저 기대한 결과를 산출하는지 **검증** 할 수 있어야 한다.
- 어떤 작은 부분의 코드에 대해 행복 경로 테스트를 할 수 없다면 그 내용을 완전히 이해하지 못한 것이다.

## [B]ICEP : 경계조건은 맞는가 ?
- 우리가 마주치는 수 많은 결함은 **모서리 사례 (corner case)** 이므로 테스트로 처리해야 한다.
- 우리가 생각해야 하는 경계조건의 예는 다음과 같다.
  - 모호하고 일관성 없는 입력값 (특수문자 등이 포함된 경우)
  - 잘못된 양식의 데이터
  - 수치적 오버플로우를 일으키는 계산
  - 널이거나 빈 값
  - 이성적인 기대값은 훨씬 벗어나는 값 (150세의 나이 등)
  - 중복을 허용해서는 안되는 목록에 중복된 값이 존재하는 경우
  - 정렬이 안된 정렬 리스트 또는 그 반대
  - 정렬 알고리즘에 이미 정렬된 값을 넣거나 역순 데이터를 넣는 경우 (퀵 정렬에 역순 데이터를 넣을 경우 성능은 최악)
  - 시간 순이 맞지 않는 경우

> 클래스 설계시 위와 같은 문제를 고려할지 여부는 전적으로 우리에게 달려 있다. <br/>
> 클래스가 외부에서 호출하는 API 이고, 클라이언트를 믿을 수 없다면 이에 대한 보호가 필요하다.

## 경계 조건에서 CORRECT 를 기억하라
- CORRECT 는 잠재적인 경계 조건을 기억하는데 도움을 준다.
- Conformance : 값이 기대한 양식을 준수하는가 ?
- Ordering : 값의 집합이 가지는 순서가 정확한가 ?
- Range : 이성적인 최소값과 최대값의 범위를 지키는가 ?
- Reference : 코드 자체에서 통제할 수 없는 외부 참조를 포함하는가 ?
- Existence : 값이 존재하는가 ? (널, 0 등..)
- Cardinality : 정확한 값이 충분히 들어있는가 ?
- Time : 기대했던 순서대로 동작하는가 ?

## B[I]CEP : 역 관계를 검사할 수 있는가 ?
- 논리적인 역 관계를 적용해 검사할 수 있다.
- 종종 수학계산에서 이를 활용함 (곱셈을 나눗셈으로 검증하고 뺄셈으로 덧셈을 검증하는 등..)
- 데이터베이스에 데이터를 넣는 코드가 있다면, JDBC 로 쿼리해보는 것도 하나의 방법

## BI[C]EP : 다른 수단을 활용해 교차 검사 할 수 있는가 ?
- 도서 대출 시스템을 예로 들어봄
- 도서관에 대한 기대사항은 어떤 때라도 모든것이 균형이 맞아야 한다.
- 대출된 도서 + 대출되지 않은 도서의 합은 각 도서의 총 수량과 일치해야 한다.
  - 서로 다른 장소에 저장될 수 있지만 모두 합하면 합이 맞아야하고, 교차 검사가 가능해야 한다.
- 클래스의 서로 다른 조각 데이터를 사용해 모든 데이터가 합산되는지 확인해보는 것도 하나의 방법이다.

## BIC[E]P : 오류 조건을 강제로 일어나게 할 수 있는가 ?
- 행복 경로가 있다면 불행 경로도 있다.
- 오류는 어떤 경우에도 발생할 수 있다.
- 테스트도 오류들을 강제로 발생시킬 수 있어야 한다.
- 좋은 단위 테스트는 코드에 존재하는 로직에 대한 커버리지를 달성하는 것이 아니다.
- 때로는 뒷 주머니에서 작은 창의력을 꺼내는 노력이 필요하다.
- 가장 끔찍한 결함들은 종종 전혀 예상치 못한 곳에서 나온다.

## BICE[P] : 성능 조건은 기준에 부합하는가 ?
- 구글의 롭 파이크는, "병목 지점은 놀라운 곳에서 일어난다, 실제 병목이 어디인지 증명되기 전까지 짐작하여 코드를 난도질 하지 말라" 라고 말한다.
- 많은 개발자는 성능 문제의 원인을 추측한다.
- 추측만으로 대응하기 보다는 단위 테스트를 설계하여 진짜 문제가 어디있으며 예상한 변경사항으로 어떤 차이가 생겼는지 파악해야 한다.
- 성능 테스트시 주의할 점은 다음과 같다.
  - 코드 블록을 충분한 횟수만큼 실행해 봐야한다.
    - 위 방법으로 타이밍과 CPU Clock Cycle 이슈를 제거한다.
  - 반복하는 코드를 JVM 이 최적화하지 못하는지 확인해야 한다.
  - 최적화되지 않은 테스트는, 수 밀리초가 걸리는 일반적인 테스트보다 느리다.
  - 느린 테스트는 빠른 테스트와 분리해야 한다.
  - 동일한 머신이라도 실행 시간은 시스템 로드처럼 잡다한 요소에 의해 달라질 수 있다.
- 단위 성능 측정을 잘하는 방법은 변경사항을 만들때 **기준점** 으로 활용하는 것이다.
- 최적화 이전에 먼저 기준점으로 현재 경과 시간을 측정하는 성능 테스트를 작성하라. (이를 여러번 실행해 보고 평균을 계산하라)
- 그 후 코드를 변경하고 성능 테스트를 실행하고 결과를 비교하라.

> 모든 성능 최적화 시도는 실제 데이터로 해야 하며 추측을 기반으로 해서는 안된다.
