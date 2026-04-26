# Input
## 候选人简历
---简历内容开始---
{resumeText}
---简历内容结束---

## 岗位 JD
---JD 内容开始---
{jdContent}
---JD 内容结束---

## 输出要求
请直接输出 JSON 对象，字段严格为：
- `skill`：选中的 skill key，必须是 `java` / `llm` / `frontend` / `system_design` / `behavioral` 之一
- `reason`：选择理由，一句话（中文，不超过 80 字）

不要输出 Markdown 代码块，不要输出任何额外文本。
