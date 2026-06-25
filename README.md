# Employee System Portfolio

원격근무·근태 관리 팀 프로젝트에서 **내가 맡은 역할**만 추려 정리한 포트폴리오입니다.
전체 팀 저장소가 아니라, 직접 구현한 부분(직원 QR 사원증 앱 · 게시판 · JWT 인증)만 포함합니다.

> 일부 코드는 팀 프로젝트의 공용 구조에서 분리해 온 것이라, 단독 실행보다는 **구현 내용 확인용**으로 봐주세요.

---

## [1] 직원 QR 사원증 앱 (`qr-employee-card-app/`)

- **Kotlin + Jetpack Compose**로 구현한 안드로이드 앱
- 로그인 후 직원 정보를 담은 **디지털 사원증** 화면 제공
- **ZXing**으로 직원 정보를 QR 코드로 생성해 출입 인증에 사용
- Flask 서버와 통신해 **로그인 · 사원 정보 조회 · 회원가입 · 이메일 중복확인** 수행

연동 백엔드 엔드포인트: `backend/App/route/` (`app_login.py`, `app_register.py`, `auth_routes.py`)

## [2] 직원·관리자 게시판 (`backend/route/Manager/`, `frontend/manager_pages/`, `frontend/user/`)

- 글 목록 / 상세 / 조회수 / 작성 · 수정 · 삭제 / 댓글 / 파일 업로드 · 다운로드
- **작성자 본인 / 관리자만** 수정·삭제 가능하도록 권한 분기
- Flask REST API + React 화면, DB에 게시글·댓글·첨부 저장

| 구분 | 파일 |
| --- | --- |
| Backend | `board_routes.py`, `notice_routes.py`, `file_routes.py` |
| Frontend | `NoticeBoardPage.jsx`, `NoticePage.jsx`, `FileBoard.jsx` |

## [3] JWT 토큰 인증 (`backend/security/`, `frontend/pages/`)

- 로그인 성공 시 역할(role) 포함 **JWT 발급(HS256)**, 클라이언트는 검증·갱신
- **데코레이터**로 권한 검사를 한 줄로 적용 (`auth_decorators.py`)
- 프론트는 토큰을 `Authorization` 헤더로 전송, **역할별 페이지로 라우팅**

| 구분 | 파일 |
| --- | --- |
| 토큰 발급/검증 | `security/jwt_utils.py` |
| 권한 데코레이터 | `security/auth_decorators.py` |
| 로그인/회원가입 화면 | `frontend/pages/Login.jsx`, `Register.jsx` |

---

## 기술 스택

- **Mobile**: Kotlin, Jetpack Compose, ZXing, Coil
- **Backend**: Python, Flask, PyJWT
- **Frontend**: React (Vite)
- **DB**: MySQL

## 보안 참고

- 비밀키·DB 비밀번호 등은 모두 `.env` 환경변수로 분리되어 있으며, 저장소에는 포함하지 않습니다.
