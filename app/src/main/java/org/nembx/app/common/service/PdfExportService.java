package org.nembx.app.common.service;


import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.resume.entity.Resume;
import org.nembx.app.module.resume.entity.res.ResumeAnalysisResponse;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PdfExportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb SECTION_COLOR = new DeviceRgb(52, 73, 94);

    /**
     * 导出简历分析报告为PDF
     */
    public byte[] exportResumeAnalysis(Resume resume, ResumeAnalysisResponse analysis) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {
            // 使用支持中文的字体
            PdfFont font = createChineseFont();
            document.setFont(font);

            // 标题
            Paragraph title = new Paragraph("简历分析报告")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(HEADER_COLOR);
            document.add(title);

            // 基本信息
            document.add(new Paragraph("\n"));
            document.add(createSectionTitle("基本信息"));
            document.add(new Paragraph("文件名: " + resume.getFileName()));
            document.add(new Paragraph("上传时间: " +
                    (resume.getUploadTime() != null ? DATE_FORMAT.format(resume.getUploadTime()) : "未知")));

            // 总分
            document.add(new Paragraph("\n"));
            document.add(createSectionTitle("综合评分"));
            Paragraph scoreP = new Paragraph("总分: " + analysis.overallScore() + " / 100")
                    .setFontSize(18)
                    .setBold()
                    .setFontColor(getScoreColor(analysis.overallScore()));
            document.add(scoreP);

            // 各维度评分
            if (analysis.scoreDetail() != null) {
                document.add(new Paragraph("\n"));
                document.add(createSectionTitle("各维度评分"));

                Table scoreTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                        .useAllAvailableWidth();
                addScoreRow(scoreTable, "项目经验", analysis.scoreDetail().projectScore(), 40);
                addScoreRow(scoreTable, "技能匹配度", analysis.scoreDetail().skillMatchScore(), 20);
                addScoreRow(scoreTable, "内容完整性", analysis.scoreDetail().contentScore(), 15);
                addScoreRow(scoreTable, "结构清晰度", analysis.scoreDetail().structureScore(), 15);
                addScoreRow(scoreTable, "表达专业性", analysis.scoreDetail().expressionScore(), 10);
                document.add(scoreTable);
            }

            // 简历摘要
            if (analysis.summary() != null) {
                document.add(new Paragraph("\n"));
                document.add(createSectionTitle("简历摘要"));
                document.add(new Paragraph(sanitizeText(analysis.summary())));
            }

            // 优势亮点
            if (analysis.strengths() != null && !analysis.strengths().isEmpty()) {
                document.add(new Paragraph("\n"));
                document.add(createSectionTitle("优势亮点"));
                for (String strength : analysis.strengths()) {
                    document.add(new Paragraph("• " + sanitizeText(strength)));
                }
            }

            // 改进建议
            if (analysis.suggestions() != null && !analysis.suggestions().isEmpty()) {
                document.add(new Paragraph("\n"));
                document.add(createSectionTitle("改进建议"));
                for (ResumeAnalysisResponse.Suggestion suggestion : analysis.suggestions()) {
                    document.add(new Paragraph("【" + suggestion.priority() + "】" + sanitizeText(suggestion.category()))
                            .setBold());
                    document.add(new Paragraph("问题: " + sanitizeText(suggestion.issue())));
                    document.add(new Paragraph("建议: " + sanitizeText(suggestion.recommendation())));
                    document.add(new Paragraph("\n"));
                }
            }
            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("导出简历分析报告失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXPORT_PDF_FAILED, "导出简历分析报告失败");
        }
    }

    /**
     * 创建支持中文的字体
     */
    private PdfFont createChineseFont() {
        try (var fontStream = getClass().getClassLoader().getResourceAsStream("fonts/ZhuqueFangsong-Regular.ttf")) {
            // 使用项目内嵌字体（保证跨平台一致性）
            if (fontStream != null) {
                byte[] fontBytes = fontStream.readAllBytes();
                log.debug("使用项目内嵌字体: fonts/ZhuqueFangsong-Regular.ttf");
                return PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            }
            // 如果字体文件不存在，抛出异常
            log.error("未找到字体文件: fonts/ZhuqueFangsong-Regular.ttf");
            throw new BusinessException(ErrorCode.EXPORT_PDF_FAILED, "字体文件缺失，请联系管理员");
        } catch (Exception e) {
            log.error("创建中文字体失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXPORT_PDF_FAILED, "创建字体失败: " + e.getMessage());
        }
    }

    /**
     * 清理文本中可能导致字体问题的字符
     */
    private String sanitizeText(String text) {
        if (text == null) return "";
        // 移除可能导致问题的特殊字符（如 emoji）
        return text.replaceAll("[\\p{So}\\p{Cs}]", "").trim();
    }


    private Paragraph createSectionTitle(String title) {
        return new Paragraph(title)
                .setFontSize(14)
                .setBold()
                .setFontColor(SECTION_COLOR)
                .setMarginTop(10);
    }

    private void addScoreRow(Table table, String dimension, int score, int maxScore) {
        table.addCell(new Cell().add(new Paragraph(dimension)));
        table.addCell(new Cell().add(new Paragraph(score + " / " + maxScore)
                .setFontColor(getScoreColor(score * 100 / maxScore))));
    }

    private DeviceRgb getScoreColor(int score) {
        if (score >= 80) return new DeviceRgb(39, 174, 96);   // 绿色
        if (score >= 60) return new DeviceRgb(241, 196, 15);  // 黄色
        return new DeviceRgb(231, 76, 60);                    // 红色
    }
}
