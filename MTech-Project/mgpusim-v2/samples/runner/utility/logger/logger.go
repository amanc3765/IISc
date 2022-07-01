package logger

import "github.com/fatih/color"

var LoggerStatus bool

var Cyan = color.New(color.FgCyan, color.Bold)
var Red = color.New(color.FgRed, color.Bold)
var Yellow = color.New(color.FgYellow, color.Bold)
var Green = color.New(color.FgGreen, color.Bold)
var Blue = color.New(color.FgBlue, color.Bold)

func PrintLog(LoggerStatus bool, logMessage string, logColor *color.Color) {
	// LoggerStatus = true
	// LoggerStatus = false
	if LoggerStatus {
		logColor.Printf(logMessage)
	}
}
