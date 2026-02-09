/**
 * PLACEHOLDER: Calendar / Date Picker Component
 * 
 * Web version uses react-day-picker
 * 
 * For React Native, implement using one of:
 * - react-native-calendars (recommended - visual calendar)
 * - @react-native-community/datetimepicker (native picker)
 * - expo-date-time-picker
 * 
 * Installation:
 * npm install react-native-calendars
 * # or
 * npx expo install @react-native-community/datetimepicker
 * 
 * Example with react-native-calendars:
 * 
 * import { Calendar } from 'react-native-calendars';
 * 
 * <Calendar
 *   onDayPress={(day) => setSelectedDate(day.dateString)}
 *   markedDates={{ [selectedDate]: { selected: true } }}
 *   theme={{
 *     backgroundColor: colors.background,
 *     calendarBackground: colors.card,
 *     textSectionTitleColor: colors.mutedForeground,
 *     selectedDayBackgroundColor: colors.primary,
 *     selectedDayTextColor: colors.primaryForeground,
 *     todayTextColor: colors.primary,
 *     dayTextColor: colors.foreground,
 *   }}
 * />
 */

import React, { useState } from 'react';
import { View, Text, Pressable, StyleSheet, ScrollView } from 'react-native';
import { ChevronLeft, ChevronRight } from 'lucide-react-native';
import { useTheme } from '../../theme/ThemeContext';
import { borderRadius, fontSize, spacing } from '../../theme';

interface CalendarProps {
    selected?: Date;
    onSelect?: (date: Date) => void;
    markedDates?: string[]; // ISO date strings
}

export function Calendar({ selected, onSelect, markedDates = [] }: CalendarProps) {
    const { colors } = useTheme();
    const [currentMonth, setCurrentMonth] = useState(selected || new Date());

    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const days: (number | null)[] = [];
    for (let i = 0; i < firstDay; i++) days.push(null);
    for (let i = 1; i <= daysInMonth; i++) days.push(i);

    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'];
    const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    const goToPrevMonth = () => setCurrentMonth(new Date(year, month - 1, 1));
    const goToNextMonth = () => setCurrentMonth(new Date(year, month + 1, 1));

    const isSelected = (day: number) => {
        if (!selected || !day) return false;
        return selected.getFullYear() === year &&
            selected.getMonth() === month &&
            selected.getDate() === day;
    };

    const isMarked = (day: number) => {
        if (!day) return false;
        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        return markedDates.includes(dateStr);
    };

    const isToday = (day: number) => {
        if (!day) return false;
        const today = new Date();
        return today.getFullYear() === year &&
            today.getMonth() === month &&
            today.getDate() === day;
    };

    return (
        <View style={[styles.container, { backgroundColor: colors.card }]}>
            {/* Header */}
            <View style={styles.header}>
                <Pressable onPress={goToPrevMonth} style={styles.navButton}>
                    <ChevronLeft size={20} color={colors.foreground} />
                </Pressable>
                <Text style={[styles.monthYear, { color: colors.foreground }]}>
                    {monthNames[month]} {year}
                </Text>
                <Pressable onPress={goToNextMonth} style={styles.navButton}>
                    <ChevronRight size={20} color={colors.foreground} />
                </Pressable>
            </View>

            {/* Day names */}
            <View style={styles.dayNamesRow}>
                {dayNames.map((name) => (
                    <Text key={name} style={[styles.dayName, { color: colors.mutedForeground }]}>
                        {name}
                    </Text>
                ))}
            </View>

            {/* Days grid */}
            <View style={styles.daysGrid}>
                {days.map((day, index) => (
                    <Pressable
                        key={index}
                        style={[
                            styles.dayCell,
                            isSelected(day!) && { backgroundColor: colors.primary },
                            isToday(day!) && !isSelected(day!) && { borderColor: colors.primary, borderWidth: 1 },
                        ]}
                        onPress={() => day && onSelect?.(new Date(year, month, day))}
                        disabled={!day}
                    >
                        {day && (
                            <>
                                <Text
                                    style={[
                                        styles.dayText,
                                        { color: isSelected(day) ? colors.primaryForeground : colors.foreground },
                                    ]}
                                >
                                    {day}
                                </Text>
                                {isMarked(day) && !isSelected(day) && (
                                    <View style={[styles.marker, { backgroundColor: colors.primary }]} />
                                )}
                            </>
                        )}
                    </Pressable>
                ))}
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        padding: spacing.md,
        borderRadius: borderRadius.xl,
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: spacing.md,
    },
    navButton: {
        padding: spacing.sm,
    },
    monthYear: {
        fontSize: fontSize.md,
        fontWeight: '600',
    },
    dayNamesRow: {
        flexDirection: 'row',
        marginBottom: spacing.sm,
    },
    dayName: {
        flex: 1,
        textAlign: 'center',
        fontSize: fontSize.xs,
        fontWeight: '500',
    },
    daysGrid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
    },
    dayCell: {
        width: '14.28%',
        aspectRatio: 1,
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: borderRadius.full,
    },
    dayText: {
        fontSize: fontSize.sm,
    },
    marker: {
        position: 'absolute',
        bottom: 4,
        width: 4,
        height: 4,
        borderRadius: 2,
    },
});
