#ifndef BACKLIGHT_H
#define BACKLIGHT_H

#include "lcd.h"
#include "scheduler.h"

extern SimpleSt7789 lcd;

namespace ThemeControl {
	namespace detail {
		uint32_t lastFlashRequest_ms  = 0;
		uint32_t offWithTimerStart_ms = 0;
		int8_t toggleCount            = -1;
		bool isLight                  = false;
		bool isLightHardware          = false;
		bool isWaitingForDark         = false;

		void writeLight(bool value) {
			if (isLightHardware == value)
				return;

			isLightHardware = value;
			value ? lcd.invertDisplay(true) : lcd.invertDisplay(false);
		}

		bool isHardwareLight() {
			return isLightHardware;
		}
	} // namespace detail

	void flashScreen() {
		if (millis() > detail::lastFlashRequest_ms + 5000) {
			detail::lastFlashRequest_ms = millis();
			detail::toggleCount         = 0;
		}
	}

	void darkWithTimer() {
		if (detail::isLight == false || detail::isWaitingForDark)
			return;
		detail::isWaitingForDark = true;

		// Cancel the flashing
		detail::toggleCount = -1;
		detail::writeLight(detail::isLight);

		detail::offWithTimerStart_ms = millis();
	}

	void light() {
		detail::isLight          = true;
		detail::toggleCount      = -1;
		detail::isWaitingForDark = false;
		detail::writeLight(true);
	}

	void dark() {
		detail::isLight          = false;
		detail::toggleCount      = -1;
		detail::isWaitingForDark = false;
		detail::writeLight(false);
	}

	void update() {
		if (detail::isWaitingForDark) {
			constexpr uint32_t TIMEOUT = 5000;
			if (millis() > detail::offWithTimerStart_ms + TIMEOUT) {
				dark();
			}
		}

		if (detail::toggleCount != -1) {
			constexpr int8_t MAX_CYCLE = 2;
			DO_EVERY(100) {
				detail::toggleCount++;
				// End of cycle, make sure the backlight is in the correct state
				if (detail::toggleCount == MAX_CYCLE * 2) {
					detail::writeLight(detail::isLight);
					detail::toggleCount = -1;
				}
				// Toggle the backlight
				else {
					detail::writeLight(!detail::isHardwareLight());
				}
			}
		}
	}
} // namespace ThemeControl

#endif // BACKLIGHT_H