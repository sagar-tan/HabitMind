import React, { useState } from 'react';
import {
    View,
    Text,
    Pressable,
    ScrollView,
    TextInput,
    StyleSheet,
    Modal,
    KeyboardAvoidingView,
    Platform,
} from 'react-native';
import {
    Plus, PenLine, Mic, Image, X, Tag, ArrowLeft,
    Clock,
} from 'lucide-react-native';
import { useApp, JournalEntry } from '../context/AppContext';
import { useTheme } from '../theme/ThemeContext';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Textarea } from '../components/ui/Textarea';
import { Input } from '../components/ui/Input';
import { borderRadius, fontSize, spacing } from '../theme';

// Entry type icons
const typeIcons = {
    text: PenLine,
    voice: Mic,
    image: Image,
};

interface EntryCardProps {
    entry: JournalEntry;
}

function EntryCard({ entry }: EntryCardProps) {
    const { colors } = useTheme();

    const time = new Date(entry.timestamp).toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
    });
    const date = new Date(entry.timestamp);
    const isToday = entry.date === new Date().toISOString().split('T')[0];

    const TypeIcon = typeIcons[entry.type];

    return (
        <Card style={styles.entryCard}>
            <View style={styles.entryContent}>
                <View style={[styles.typeIcon, { backgroundColor: colors.surface }]}>
                    <TypeIcon size={16} color={colors.primary} />
                </View>
                <View style={styles.entryText}>
                    <View style={styles.entryMeta}>
                        <Clock size={12} color={colors.mutedForeground} />
                        <Text style={[styles.entryTime, { color: colors.mutedForeground }]}>
                            {isToday ? time : date.toLocaleDateString([], { month: 'short', day: 'numeric' })}
                        </Text>
                    </View>
                    <Text
                        style={[styles.entryBody, { color: `${colors.foreground}E6` }]}
                        numberOfLines={3}
                    >
                        {entry.content}
                    </Text>
                    {entry.tags.length > 0 && (
                        <View style={styles.tagsList}>
                            {entry.tags.map((tag) => (
                                <View
                                    key={tag}
                                    style={[styles.tag, { backgroundColor: `${colors.primary}15` }]}
                                >
                                    <Text style={[styles.tagText, { color: colors.primary }]}>{tag}</Text>
                                </View>
                            ))}
                        </View>
                    )}
                </View>
            </View>
        </Card>
    );
}

interface JournalEntryCreationProps {
    visible: boolean;
    onClose: () => void;
    onSave: (entry: Omit<JournalEntry, 'id'>) => void;
}

function JournalEntryCreation({ visible, onClose, onSave }: JournalEntryCreationProps) {
    const { colors } = useTheme();
    const [content, setContent] = useState('');
    const [tags, setTags] = useState<string[]>([]);
    const [tagInput, setTagInput] = useState('');
    const [entryType, setEntryType] = useState<'text' | 'voice' | 'image'>('text');

    const addTag = () => {
        if (tagInput.trim() && !tags.includes(tagInput.trim())) {
            setTags([...tags, tagInput.trim()]);
            setTagInput('');
        }
    };

    const handleSave = () => {
        if (content.trim()) {
            onSave({
                type: entryType,
                content: content.trim(),
                tags,
                timestamp: new Date().toISOString(),
                date: new Date().toISOString().split('T')[0],
            });
            setContent('');
            setTags([]);
            setTagInput('');
            onClose();
        }
    };

    const entryTypes = [
        { type: 'text' as const, icon: PenLine, label: 'Text' },
        { type: 'voice' as const, icon: Mic, label: 'Voice' },
        { type: 'image' as const, icon: Image, label: 'Photo' },
    ];

    return (
        <Modal visible={visible} animationType="slide" presentationStyle="fullScreen">
            <KeyboardAvoidingView
                style={[styles.modalContainer, { backgroundColor: colors.background }]}
                behavior={Platform.OS === 'ios' ? 'padding' : undefined}
            >
                {/* Header */}
                <View style={styles.modalHeader}>
                    <Pressable
                        style={[styles.backButton, { backgroundColor: colors.surface }]}
                        onPress={onClose}
                    >
                        <ArrowLeft size={20} color={colors.foreground} />
                    </Pressable>
                    <Text style={[styles.modalTitle, { color: colors.foreground }]}>New Entry</Text>
                    <Button
                        size="sm"
                        variant={content.trim() ? 'default' : 'secondary'}
                        disabled={!content.trim()}
                        onPress={handleSave}
                    >
                        Save
                    </Button>
                </View>

                {/* Type selector */}
                <View style={styles.typeSelector}>
                    {entryTypes.map((item) => (
                        <Pressable
                            key={item.type}
                            style={[
                                styles.typeButton,
                                {
                                    backgroundColor:
                                        entryType === item.type ? `${colors.primary}20` : colors.surface,
                                    borderColor: entryType === item.type ? `${colors.primary}30` : 'transparent',
                                },
                            ]}
                            onPress={() => setEntryType(item.type)}
                        >
                            <item.icon
                                size={16}
                                color={entryType === item.type ? colors.primary : colors.mutedForeground}
                            />
                            <Text
                                style={[
                                    styles.typeLabel,
                                    { color: entryType === item.type ? colors.primary : colors.mutedForeground },
                                ]}
                            >
                                {item.label}
                            </Text>
                        </Pressable>
                    ))}
                </View>

                {/* Content */}
                <View style={styles.contentSection}>
                    {entryType === 'text' && (
                        <Textarea
                            value={content}
                            onChangeText={setContent}
                            placeholder="What's on your mind?"
                            style={styles.textarea}
                        />
                    )}
                    {entryType === 'voice' && (
                        <View style={styles.voiceSection}>
                            <View style={[styles.micButton, { backgroundColor: `${colors.primary}20` }]}>
                                <Mic size={32} color={colors.primary} />
                            </View>
                            <Text style={[styles.voiceHint, { color: colors.mutedForeground }]}>
                                Tap to record
                            </Text>
                            <Input
                                value={content}
                                onChangeText={setContent}
                                placeholder="Or type a note about the recording..."
                                style={styles.voiceInput}
                            />
                        </View>
                    )}
                    {entryType === 'image' && (
                        <View style={styles.imageSection}>
                            <Pressable
                                style={[styles.imagePlaceholder, { borderColor: colors.border }]}
                            >
                                <Image size={32} color={`${colors.mutedForeground}66`} />
                                <Text style={[styles.imageHint, { color: colors.mutedForeground }]}>
                                    Tap to add photo
                                </Text>
                            </Pressable>
                            <Input
                                value={content}
                                onChangeText={setContent}
                                placeholder="Add a caption..."
                            />
                        </View>
                    )}
                </View>

                {/* Tags */}
                <View style={styles.tagsSection}>
                    <View style={styles.tagInputRow}>
                        <Tag size={16} color={colors.mutedForeground} />
                        <TextInput
                            value={tagInput}
                            onChangeText={setTagInput}
                            onSubmitEditing={addTag}
                            placeholder="Add tag..."
                            placeholderTextColor={`${colors.mutedForeground}66`}
                            style={[styles.tagInput, { color: colors.foreground }]}
                            returnKeyType="done"
                        />
                    </View>
                    {tags.length > 0 && (
                        <View style={styles.tagsRow}>
                            {tags.map((tag) => (
                                <Pressable
                                    key={tag}
                                    style={[styles.tagChip, { backgroundColor: `${colors.primary}15` }]}
                                    onPress={() => setTags(tags.filter((t) => t !== tag))}
                                >
                                    <Text style={[styles.tagChipText, { color: colors.primary }]}>{tag}</Text>
                                    <X size={14} color={colors.primary} />
                                </Pressable>
                            ))}
                        </View>
                    )}
                </View>
            </KeyboardAvoidingView>
        </Modal>
    );
}

export function JournalScreen() {
    const { colors } = useTheme();
    const { journal, addJournalEntry } = useApp();
    const [showCreate, setShowCreate] = useState(false);

    return (
        <View style={[styles.container, { backgroundColor: colors.background }]}>
            <ScrollView contentContainerStyle={styles.scrollContent}>
                {/* Header */}
                <View style={styles.header}>
                    <Text style={[styles.title, { color: colors.foreground }]}>Journal</Text>
                    <Text style={[styles.count, { color: colors.mutedForeground }]}>
                        {journal.length} entries
                    </Text>
                </View>

                {/* Empty state or entries */}
                {journal.length === 0 ? (
                    <View style={styles.emptyState}>
                        <View style={[styles.emptyIcon, { backgroundColor: colors.surface }]}>
                            <PenLine size={32} color={`${colors.mutedForeground}80`} />
                        </View>
                        <Text style={[styles.emptyTitle, { color: colors.mutedForeground }]}>
                            Your journal is empty
                        </Text>
                        <Text style={[styles.emptySubtitle, { color: `${colors.mutedForeground}99` }]}>
                            Start capturing your thoughts
                        </Text>
                        <Button onPress={() => setShowCreate(true)}>Write first entry</Button>
                    </View>
                ) : (
                    <View style={styles.entriesList}>
                        {journal.map((entry) => (
                            <EntryCard key={entry.id} entry={entry} />
                        ))}
                    </View>
                )}
            </ScrollView>

            {/* FAB */}
            <Pressable
                style={[styles.fab, { backgroundColor: colors.primary }]}
                onPress={() => setShowCreate(true)}
            >
                <Plus size={24} color={colors.primaryForeground} />
            </Pressable>

            {/* Create Entry Modal */}
            <JournalEntryCreation
                visible={showCreate}
                onClose={() => setShowCreate(false)}
                onSave={addJournalEntry}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    scrollContent: {
        padding: spacing.lg,
        paddingBottom: spacing['5xl'],
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: spacing.lg,
    },
    title: {
        fontSize: fontSize['2xl'],
        fontWeight: '600',
    },
    count: {
        fontSize: fontSize.sm,
    },
    entriesList: {
        gap: spacing.md,
    },
    entryCard: {
        marginBottom: spacing.md,
    },
    entryContent: {
        flexDirection: 'row',
        padding: spacing.lg,
        gap: spacing.md,
    },
    typeIcon: {
        width: 36,
        height: 36,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        justifyContent: 'center',
    },
    entryText: {
        flex: 1,
    },
    entryMeta: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.xs,
        marginBottom: spacing.xs,
    },
    entryTime: {
        fontSize: fontSize.xs,
    },
    entryBody: {
        fontSize: fontSize.sm,
        lineHeight: fontSize.sm * 1.5,
        marginBottom: spacing.sm,
    },
    tagsList: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: spacing.xs,
    },
    tag: {
        paddingHorizontal: spacing.sm,
        paddingVertical: 2,
        borderRadius: borderRadius.md,
    },
    tagText: {
        fontSize: 10,
    },
    emptyState: {
        alignItems: 'center',
        paddingVertical: spacing['5xl'],
    },
    emptyIcon: {
        width: 64,
        height: 64,
        borderRadius: borderRadius['2xl'],
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: spacing.lg,
    },
    emptyTitle: {
        fontSize: fontSize.md,
        marginBottom: spacing.xs,
    },
    emptySubtitle: {
        fontSize: fontSize.sm,
        marginBottom: spacing.lg,
    },
    fab: {
        position: 'absolute',
        bottom: spacing['3xl'],
        right: spacing.xl,
        width: 56,
        height: 56,
        borderRadius: 28,
        alignItems: 'center',
        justifyContent: 'center',
        elevation: 4,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.25,
        shadowRadius: 4,
    },
    // Modal styles
    modalContainer: {
        flex: 1,
    },
    modalHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: spacing.lg,
        paddingTop: spacing['2xl'],
    },
    backButton: {
        width: 40,
        height: 40,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        justifyContent: 'center',
    },
    modalTitle: {
        fontSize: fontSize.lg,
        fontWeight: '600',
    },
    typeSelector: {
        flexDirection: 'row',
        gap: spacing.sm,
        paddingHorizontal: spacing.lg,
        marginBottom: spacing.lg,
    },
    typeButton: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.sm,
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.sm,
        borderRadius: borderRadius.xl,
        borderWidth: 1,
    },
    typeLabel: {
        fontSize: fontSize.sm,
    },
    contentSection: {
        flex: 1,
        paddingHorizontal: spacing.lg,
    },
    textarea: {
        height: 200,
    },
    voiceSection: {
        alignItems: 'center',
        paddingTop: spacing['2xl'],
    },
    micButton: {
        width: 80,
        height: 80,
        borderRadius: 40,
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: spacing.lg,
    },
    voiceHint: {
        fontSize: fontSize.sm,
        marginBottom: spacing.lg,
    },
    voiceInput: {
        width: '100%',
    },
    imageSection: {
        gap: spacing.lg,
    },
    imagePlaceholder: {
        aspectRatio: 16 / 9,
        borderRadius: borderRadius['2xl'],
        borderWidth: 2,
        borderStyle: 'dashed',
        alignItems: 'center',
        justifyContent: 'center',
        gap: spacing.sm,
    },
    imageHint: {
        fontSize: fontSize.sm,
    },
    tagsSection: {
        padding: spacing.lg,
    },
    tagInputRow: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.sm,
        marginBottom: spacing.md,
    },
    tagInput: {
        flex: 1,
        fontSize: fontSize.sm,
        paddingVertical: spacing.sm,
    },
    tagsRow: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: spacing.sm,
    },
    tagChip: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.xs,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.sm,
        borderRadius: borderRadius.lg,
    },
    tagChipText: {
        fontSize: fontSize.xs,
    },
});
