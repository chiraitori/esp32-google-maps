/**
 * ST7789 display registers
 */

// Table 1
#define REG_NOP       0x00 // No Operation
#define REG_SWRESET   0x01 // Software Reset
#define REG_RDDID     0x04 // Read Display ID
#define REG_RDDST     0x09 // Read Display Status
#define REG_RDDPM     0x0A // Read Display Power Mode
#define REG_RDDMADCTL 0x0B // Read Display MADCTL
#define REG_RDDCOLMOD 0x0C // Read Display Pixel Format
#define REG_RDDIM     0x0D // Read Display Image Mode
#define REG_RDDSM     0x0E // Read Display Signal Mode
#define REG_RDDSR     0x0F // Read Display Self-Diagnostic Result (ST7789V)
#define REG_SLPIN     0x10 // Sleep In
#define REG_SLPOUT    0x11 // Sleep Out
#define REG_PTLON     0x12 // Partial Display Mode ON
#define REG_NORON     0x13 // Normal Display Mode ON

#define REG_INVOFF  0x20 // Display Inversion OFF
#define REG_INVON   0x21 // Display Inversion ON
#define REG_GAMSET  0x26 // Gamma Set
#define REG_DISPOFF 0x28 // Display OFF
#define REG_DISPON  0x29 // Display ON
#define REG_CASET   0x2A // Column Address Set
#define REG_RASET   0x2B // Row Address Set
#define REG_RAMWR   0x2C // Memory Write
#define REG_RAMRD   0x2E // Memory Read

#define REG_PTLAR    0x30 // Partial Area
#define REG_VSCRDEF  0x33 // Vertical Scrolling Definition (ST7789V)
#define REG_TEOFF    0x34 // Tearing Effect Line OFF
#define REG_TEON     0x35 // Tearing Effect Line ON
#define REG_MADCTL   0x36 // Memory Data Access Control
#define REG_VSCSAD   0x37 // Vertical Scrolling Start Address
#define REG_IDMOFF   0x38 // Idle Mode OFF
#define REG_IDMON    0x39 // Idle Mode ON
#define REG_COLMOD   0x3A // Interface Pixel Format
#define REG_WRMEMC   0x3C // Memory Write Continue
#define REG_RDMEMC   0x3E // Memory Read Continue
#define REG_STE      0x44 // Set Tear Scanline
#define REG_GSCAN    0x45 // Get Scanline
#define REG_WRDISBV  0x51 // Write Display Brightness
#define REG_RDDISBV  0x52 // Read Display Brightness Value
#define REG_WRCTRLD  0x53 // Write CTRL Display
#define REG_RDCTRLD  0x54 // Read CTRL Display
#define REG_WRCACE   0x55 // Write Content Adaptive Brightness Control and Color Enhancement
#define REG_RDCABC   0x56 // Read Content Adaptive Brightness Control
#define REG_WRCABCMB 0x5E // Write CABC Minimum Brightness
#define REG_RDCABCMB 0x5F // Read CABC Minimum Brightness
#define REG_RDABCSDR 0x68 // Read Automatic Brightness Control Self-Diagnostic Result
#define REG_RDID1    0xDA // Read ID1
#define REG_RDID2    0xDB // Read ID2
#define REG_RDID3    0xDC // Read ID3

// Table 2
#define REG_RAMCTRL    0xB0 // RAM Control
#define REG_RGBCTRL    0xB1 // RGB Interface Control
#define REG_PORCTRL    0xB2 // Porch Setting
#define REG_FRCTRL1    0xB3 // Frame Rate Control 1
#define REG_PARCTRL    0xB5 // Partial Display Control
#define REG_GCTRL      0xB7 // Gate Control
#define REG_GTADJ      0xB8 // Gate On Timing Adjustment
#define REG_DGMEN      0xBA // Digital Gamma Enable
#define REG_VCOMS      0xBB // VCOM Setting
#define REG_POWSAVE    0xBC // Power Saving Mode
#define REG_DLPOFFSAVE 0xBD // Display off Power Save
#define REG_LCMCTRL    0xC0 // LCM Control
#define REG_IDSET      0xC1 // ID Code Setting
#define REG_VDVVRHEN   0xC2 // VDV and VRH Command Enable
#define REG_VRHS       0xC3 // VRH Set
#define REG_VDVS       0xC4 // VDV Set
#define REG_VCMOFSET   0xC5 // VCOM Offset Set
#define REG_FRCTR2     0xC6 // Frame Rate Control in Normal Mode
#define REG_CABCCTRL   0xC7 // CABC Control
#define REG_REGSEL1    0xC8 // Register Value Section 1
#define REG_REGSEL2    0xCA // Register Value Section 2
#define REG_PWMFRSEL   0xCC // PWM Frequency Selection
#define REG_PWCTRL1    0xD0 // Power Control 1
#define REG_VAPVANEN   0xD2 // Enable VAP/VAN signal output
#define REG_CMD2EN     0xDF // Command 2 Enable
#define REG_PVGAMCTRL  0xE0 // Positive Voltage Gamma Control
#define REG_NVGAMCTRL  0xE1 // Negative Voltage Gamma Control
#define REG_DGMLUTR    0xE2 // Digital Gamma Look-up Table for Red
#define REG_DGMLUTB    0xE3 // Digital Gamma Look-up Table for Blue
#define REG_GATECTRL   0xE4 // Gate Control
#define REG_SPI2EN     0xE7 // SPI2 Enable
#define REG_PWCTRL2    0xE8 // Power Control 2
#define REG_EQCTRL     0xE9 // Equalize time control
#define REG_PROMCTRL   0xEC // Program Mode Control
#define REG_PROMEN     0xFA // Program Mode Enable
#define REG_NVMSET     0xFC // NVM Setting
#define REG_PROMACT    0xFE // Program action

#define MADCTL_MY  0x80
#define MADCTL_MX  0x40
#define MADCTL_MV  0x20
#define MADCTL_ML  0x10
#define MADCTL_RGB 0x00
