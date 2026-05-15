# Deploy Spring Boot lên AWS Lambda, sponsor bởi claude (80%)

## Bước 0: Cấu hình Access Key

Cấu hình access key trong máy trước, tạo IAM user rồi tạo access key và tải về sử dụng.

<img width="1280" height="617" alt="lambda+API" src="https://github.com/user-attachments/assets/cfa98a46-caa9-4459-a273-b7c0ca95e3a3" />

## Lựa chọn Strategy

**Option A: aws-serverless-java-container** — vì nó là câu trả lời lý tưởng.

## Bước 1: Chỉnh `pom.xml`

File `pom.xml` thêm thư viện để Lambda "nói chuyện" được với Spring Boot, và đổi cách đóng gói JAR từ "nested" sang "flat" để Lambda đọc được.

Vào trong `pom.xml`, thêm vào:

- **`aws-serverless-java-container-springboot3`** — lib AWS, chuyển đổi giữa Lambda event (API Gateway HTTP request) <-> Spring Boot servlet request. Nó boot Spring context bên trong Lambda.
- **`maven-shade-plugin`** — tạo ra "uber JAR" (flat JAR chứa tất cả classes). Lambda KHÔNG hiểu được Spring Boot fat JAR format (nested JARs), nên phải dùng shade để flatten tất cả dependencies ra cùng 1 level.
- **Transformers** — xử lý merge các file config của Spring khi gộp nhiều JAR lại (nếu không merge sẽ bị ghi đè và Spring không tìm thấy beans).

## Bước 2: Tạo `StreamLambdaHandler.java`

Tạo file `StreamLambdaHandler.java` ở `src/main/java/dev/byol/lambda/`.

`StreamLambdaHandler.java` đóng vai trò như 1 entry point, AWS Lambda sẽ gọi vào mỗi khi có HTTP request:

- **static block:** Khởi tạo Spring Boot context 1 lần duy nhất khi Lambda cold start. Sau đó context được giữ lại cho các request tiếp theo (warm start).
- **handleRequest():** Nhận raw input stream từ API Gateway → đẩy qua `SpringBootLambdaContainerHandler` → handler chuyển thành request Spring MVC hiểu → Spring route đến đúng Controller → trả response ra output stream.

## Bước 3: Điều chỉnh `template.yaml`

```yaml
Handler: TODO_FILL_IN → Handler: dev.byol.lambda.StreamLambdaHandler::handleRequest
```

Để Lambda biết gọi class nào khi request đến. Sử dụng format: `fully.qualified.ClassName::methodName`. Lambda runtime sẽ load class `dev.byol.lambda.StreamLambdaHandler`, rồi gọi method `handleRequest`.

## Bước 4: Build & Deploy

Sau khi config xong, mở PowerShell, navigate tới folder project:

1. Chạy `mvn package` để tạo JAR (shade)
2. Bỏ vào `.aws-sam/build/`
3. Chạy `sam deploy`

Sử dụng CloudFormation để chuyển từ code sang deploy services.

## Kết quả

| Endpoint | Kết quả |
|----------|---------|
| `GET /` | `{"ok":true,"message":"hello from your existing app","runtime":"spring-boot"}` |
| `GET /api/hello/World` | `{"greeting":"Hello, World!","timestamp":"..."}` |
| `POST /api/echo` | `{"echo":{...body bạn gửi...}}` |

**URL:** https://94clg70qlf.execute-api.us-west-2.amazonaws.com/

## Flow

```
Người dùng vào URL
    → API Gateway nhận HTTP request, chuyển thành Lambda event
    → Lambda gọi StreamLambdaHandler để handle request
    → aws-serverless-java-container dịch các event (HttpServletRequest)
    → Spring Boot xử lý logic, trả về JSON
    → Ngược lại về người dùng
```

## Các tool đã dùng

| Tool | Vai trò |
|------|---------|
| Java 21 | Ngôn ngữ lập trình |
| Maven | Build tool — compile code + đóng gói JAR |
| AWS SAM CLI | Deploy serverless app lên AWS |
| AWS CloudFormation | Tạo infrastructure tự động từ file YAML |
| AWS S3 | Lưu trữ JAR file đã upload |
| AWS Lambda | Chạy code không cần server |
| AWS API Gateway | Tạo URL công khai trên internet |


