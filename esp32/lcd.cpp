#include "lcd.h"
#include "registers.h"
#include <SPI.h>

SimpleSt7789::SimpleSt7789(SPIClass* spi,
                           const SPISettings& spiSettings,
                           uint16_t width,
                           uint16_t height,
                           uint8_t cs,
                           uint8_t dc,
                           uint8_t rst,
                           uint8_t backlight,
                           Rotation rotation)
: _spi(spi), _spiSettings(spiSettings), _width(width), _height(height), _pinCs(cs), _pinDc(dc), _pinRst(rst),
  _pinBacklight(backlight), _rotation(rotation), _xOffset(0), _yOffset(0) {
}

void SimpleSt7789::init() {
	pinMode(_pinCs, OUTPUT);
	pinMode(_pinDc, OUTPUT);

	if (_pinRst != -1) {
		pinMode(_pinRst, OUTPUT);
	}

	if (_pinBacklight != -1) {
		ledcAttach(_pinBacklight, 1000, 10);
		ledcWrite(_pinBacklight, 100);
	}

	reset();

	sendCommand(REG_SLPOUT);
	delay(120);
	setRotation(_rotation);

	sendCommandFixed(REG_COLMOD, {0x05});
	sendCommandFixed(REG_RAMCTRL, {0x00, 0xE8});
	sendCommandFixed(REG_PORCTRL, {0x0C, 0x0C, 0x00, 0x33, 0x33});
	sendCommandFixed(REG_GCTRL, {0x35});
	sendCommandFixed(REG_VCOMS, {0x35});
	sendCommandFixed(REG_LCMCTRL, {0x2C});
	sendCommandFixed(REG_VDVVRHEN, {0x01});
	sendCommandFixed(REG_VRHS, {0x13});
	sendCommandFixed(REG_VDVS, {0x20});
	sendCommandFixed(REG_FRCTR2, {0x0F});
	sendCommandFixed(REG_PWCTRL1, {0xA4, 0xA1});
	sendCommandFixed(0xD6, {0xA1});
	sendCommandFixed(REG_PVGAMCTRL, {0xF0, 0x00, 0x04, 0x04, 0x04, 0x05, 0x29, 0x33, 0x3E, 0x38, 0x12, 0x12, 0x28, 0x30});
	sendCommandFixed(REG_NVGAMCTRL, {0xF0, 0x07, 0x0A, 0x0D, 0x0B, 0x07, 0x28, 0x33, 0x3E, 0x36, 0x14, 0x14, 0x29, 0x32});
	sendCommand(REG_INVON);
	sendCommand(REG_SLPOUT);
	delay(120);
	sendCommand(REG_DISPON);

	setBrightness(100);
}

void SimpleSt7789::reset() {
	if (_pinRst == -1)
		return;

	digitalWrite(_pinCs, LOW);
	delay(50);
	digitalWrite(_pinRst, LOW);
	delay(50);
	digitalWrite(_pinRst, HIGH);
	delay(50);
}

void SimpleSt7789::setRotation(Rotation rotation) {
	uint8_t madctl = 0;
	switch (rotation) {
	case ROTATION_0: madctl = MADCTL_MX | MADCTL_MY | MADCTL_RGB; break;
	case ROTATION_90: madctl = MADCTL_MY | MADCTL_MV | MADCTL_RGB; break;
	case ROTATION_180: madctl = MADCTL_RGB; break;
	case ROTATION_270: madctl = MADCTL_MX | MADCTL_MV | MADCTL_RGB; break;
	}

	sendCommand(REG_MADCTL, &madctl, 1);
}

void SimpleSt7789::setOffset(uint16_t xOffset, uint16_t yOffset) {
	_xOffset = xOffset;
	_yOffset = yOffset;
}

void SimpleSt7789::setBrightness(uint8_t percent) {
	if (_pinBacklight == -1)
		return;

	percent = constrain(percent, 0, 100);
	ledcWrite(_pinBacklight, percent * 10);
}

void SimpleSt7789::flushWindow(uint16_t x1, uint16_t y1, uint16_t x2, uint16_t y2, uint16_t* color) {
	const auto w        = x2 - x1 + 1;
	const auto h        = y2 - y1 + 1;
	const auto numBytes = w * h * sizeof(uint16_t);
	setAddrWindow(x1, y1, x2, y2);
	sendData((const uint8_t*)color, numBytes);
}

void SimpleSt7789::invertDisplay(bool invert) {
	sendCommand(invert ? REG_INVON : REG_INVOFF);
}

void SimpleSt7789::setAddrWindow(uint16_t x1, uint16_t y1, uint16_t x2, uint16_t y2) {
	const auto ox1 = x1 + _xOffset;
	const auto ox2 = x2 + _xOffset;
	const auto oy1 = y1 + _yOffset;
	const auto oy2 = y2 + _yOffset;

	if (_rotation == ROTATION_180 || _rotation == ROTATION_270) {
		sendCommandFixed(REG_CASET, {(uint8_t)((ox1) >> 8), (uint8_t)(ox1), (uint8_t)(ox2 >> 8), (uint8_t)(ox2)});
		sendCommandFixed(REG_RASET, {(uint8_t)((oy1) >> 8), (uint8_t)(oy1), (uint8_t)(oy2 >> 8), (uint8_t)(oy2)});
	} else {
		sendCommandFixed(REG_CASET, {(uint8_t)((oy1) >> 8), (uint8_t)(oy1), (uint8_t)(oy2 >> 8), (uint8_t)(oy2)});
		sendCommandFixed(REG_RASET, {(uint8_t)((ox1) >> 8), (uint8_t)(ox1), (uint8_t)(ox2 >> 8), (uint8_t)(ox2)});
	}
	sendCommand(REG_RAMWR);
}

void SimpleSt7789::sendCommand(uint8_t command, const uint8_t* data, size_t size) {
	_spi->beginTransaction(_spiSettings);
	digitalWrite(_pinCs, LOW);
	digitalWrite(_pinDc, LOW);
	_spi->transfer(command);
	if (data && size) {
		digitalWrite(_pinDc, HIGH);
		_spi->transferBytes(data, nullptr, size);
	}
	digitalWrite(_pinCs, HIGH);
	_spi->endTransaction();
}

void SimpleSt7789::sendData(const uint8_t* data, size_t size) {
	_spi->beginTransaction(_spiSettings);
	digitalWrite(_pinCs, LOW);
	digitalWrite(_pinDc, HIGH);
	_spi->transferBytes(data, nullptr, size);
	digitalWrite(_pinCs, HIGH);
	_spi->endTransaction();
}
