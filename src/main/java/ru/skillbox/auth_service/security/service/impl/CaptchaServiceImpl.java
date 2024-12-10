package ru.skillbox.auth_service.security.service.impl;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.skillbox.auth_service.security.service.CaptchaService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl extends DefaultKaptcha implements CaptchaService {

    private String captchaCode;

    @SneakyThrows
    public byte[] generateCaptchaImage() {

        BufferedImage captchaImage = createImage(captchaCode);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ImageIO.write(captchaImage, "png", os);

        return os.toByteArray();
    }

    public String generateCaptcha() {

        Properties properties = new Properties();

        properties.setProperty(Constants.KAPTCHA_WORDRENDERER_IMPL,"com.google.code.kaptcha.text.impl.DefaultWordRenderer");
        properties.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL,"com.google.code.kaptcha.impl.WaterRipple");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE,"50");
        properties.setProperty(Constants.KAPTCHA_BORDER_COLOR,"yellow");
        properties.setProperty(Constants.KAPTCHA_BORDER_THICKNESS,"20");
        properties.setProperty(Constants.KAPTCHA_BACKGROUND_IMPL,"com.google.code.kaptcha.impl.DefaultBackground");
        properties.setProperty(Constants.KAPTCHA_BORDER,"yes");
        properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT,"100");
        properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH,"200");

        setConfig(new Config(properties));

        captchaCode = createText();

        return captchaCode;
    }
}