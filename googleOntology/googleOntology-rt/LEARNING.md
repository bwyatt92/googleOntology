# Simple Learning Feature

## What is this?

This is a **tiny first step** into ML/AI in Niagara. It shows that:
- Niagara can learn from user feedback
- Models can improve over time
- No cloud services needed!

## How it works

```
User sees: "DAT" matched to "discharge_air_temperature" (confidence: 65)
User corrects: Actually, it should be "discharge_air_temperature_sensor"

SimpleLearner stores this correction
Next time it sees "DAT" → confidence boost!
After 3-4 corrections, confidence reaches 85+
```

## The Learning Algorithm (Super Simple!)

```
1. User submits correction
2. Extract pattern (e.g., "dat" → "temperature_sensor")
3. Store pattern with count
4. When matching similar points:
   - If pattern matches: boost confidence by 5 per correction (max +20)
```

## Try It Out

### Step 1: See current matching
```bash
curl http://localhost:8080/googleOntology/v1/entities
```

### Step 2: Submit a correction (coming soon!)
```bash
POST /v1/learning/correct
{
  "originalName": "DAT",
  "wrongMatch": "discharge_air_temperature",
  "correctMatch": "discharge_air_temperature_sensor"
}
```

### Step 3: Rebuild index and see improved matching
```bash
# In Workbench: invoke rebuildIndex() action on GoogleOntologyService
```

### Step 4: Check learning stats
```bash
GET /v1/learning/stats
{
  "corrections": 5,
  "patterns": 3,
  "stats": "Learned from 5 corrections, 3 patterns discovered"
}
```

## What Makes This "Machine Learning"?

1. **Training Data**: User corrections
2. **Features**: Token patterns in point names
3. **Model**: Confidence adjustment weights
4. **Inference**: Applying learned weights to new points
5. **Improvement**: Gets better with more corrections

## Next Steps (Your On-Ramp)

### Level 1: You are here! ✅
- Basic pattern matching
- Simple statistics
- ~100 lines of code

### Level 2: Add persistence
- Save corrections to file
- Load learned patterns on startup
- Survive station restarts

### Level 3: Better learning
- Use Java ML library (Smile, DeepLearning4J)
- Decision trees or naive Bayes
- More sophisticated features

### Level 4: Real-time learning
- Update matcher without rebuilding
- Incremental learning
- A/B testing

### Level 5: Advanced ML
- Embed ONNX models
- Connect to external ML services
- Multi-agent architecture

## Why Start Small?

- ✅ See immediate results
- ✅ Understand fundamentals
- ✅ No external dependencies
- ✅ Easy to explain to stakeholders
- ✅ Foundation for bigger things

**Remember:** Even Google started with PageRank (just counting links)!
